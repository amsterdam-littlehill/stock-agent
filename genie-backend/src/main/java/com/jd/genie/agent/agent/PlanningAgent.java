package com.jd.genie.agent.agent;

import com.jd.genie.agent.dto.Message;
import com.jd.genie.agent.dto.tool.ToolCall;
import com.jd.genie.agent.dto.tool.ToolChoice;
import com.jd.genie.agent.enums.AgentState;
import com.jd.genie.agent.enums.RoleType;
import com.jd.genie.agent.llm.LLM;
import com.jd.genie.agent.prompt.PlanningPrompt;
import com.jd.genie.agent.tool.BaseTool;
import com.jd.genie.agent.tool.common.PlanningTool;
import com.jd.genie.agent.util.FileUtil;
import com.jd.genie.agent.util.SpringContextHolder;
import com.jd.genie.config.GenieConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 规划代理 - 创建和管理任务计划的代理
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class PlanningAgent extends ReActAgent {

    private List<ToolCall> toolCalls;
    private Integer maxObserve;
    private PlanningTool planningTool = new PlanningTool();
    private Boolean isColseUpdate;
    private String systemPromptSnapshot;
    private String nextStepPromptSnapshot;
    private String planId;

    public PlanningAgent(AgentContext context) {
        setName("planning");
        setDescription("An agent that creates and manages plans to solve tasks");
        ApplicationContext applicationContext = SpringContextHolder.getApplicationContext();
        GenieConfig genieConfig = applicationContext.getBean(GenieConfig.class);

        StringBuilder toolPrompt = new StringBuilder();
        for (BaseTool tool : context.getToolCollection().getToolMap().values()) {
            toolPrompt.append(String.format("工具名：%s 工具描述：%s\n", tool.getName(), tool.getDescription()));
        }

        String promptKey = "default";
        String nextPromptKey = "default";
        setSystemPrompt(genieConfig.getPlannerSystemPromptMap().getOrDefault(promptKey, PlanningPrompt.SYSTEM_PROMPT)
                .replace("{{tools}}", toolPrompt.toString())
                .replace("{{query}}", context.getQuery())
                .replace("{{date}}", context.getDateInfo())
                .replace("{{sopPrompt}}", context.getSopPrompt()));
        setNextStepPrompt(genieConfig.getPlannerNextStepPromptMap().getOrDefault(nextPromptKey, PlanningPrompt.NEXT_STEP_PROMPT)
                .replace("{{tools}}", toolPrompt.toString())
                .replace("{{query}}", context.getQuery())
                .replace("{{date}}", context.getDateInfo())
                .replace("{{sopPrompt}}", context.getSopPrompt()));

        setSystemPromptSnapshot(getSystemPrompt());
        setNextStepPromptSnapshot(getNextStepPrompt());

        setPrinter(context.printer);
        setMaxSteps(genieConfig.getPlannerMaxSteps());
        setLlm(new LLM(genieConfig.getPlannerModelName(), ""));

        setContext(context);
        setIsColseUpdate("1".equals(genieConfig.getPlanningCloseUpdate()));

        // 初始化工具集合
        availableTools.addTool(planningTool);
        planningTool.setAgentContext(context);
    }

    @Override
    public boolean think() {
        long startTime = System.currentTimeMillis();
        // 获取文件内容
        String filesStr = FileUtil.formatFileInfo(context.getProductFiles(), false);
        setSystemPrompt(getSystemPromptSnapshot().replace("{{files}}", filesStr));
        setNextStepPrompt(getNextStepPromptSnapshot().replace("{{files}}", filesStr));
        log.info("{} planer fileStr {}", context.getRequestId(), filesStr);

        // 关闭了动态更新Plan，直接执行下一个task
        if (isColseUpdate) {
            if (Objects.nonNull(planningTool.getPlan())) {
                planningTool.stepPlan();
                return true;
            }
        }

        try {
            if (!getMemory().getLastMessage().getRole().equals(RoleType.USER)) {
                Message userMsg = Message.userMessage(getNextStepPrompt(), null);
                getMemory().addMessage(userMsg);
            }

            context.setStreamMessageType("plan_thought");
            CompletableFuture<LLM.ToolCallResponse> future = getLlm().askTool(context,
                    getMemory().getMessages(),
                    Message.systemMessage(getSystemPrompt(), null),
                    availableTools,
                    ToolChoice.AUTO, null, context.getIsStream(), 300
            );

            LLM.ToolCallResponse response = future.get();
            setToolCalls(response.getToolCalls());

            // 记录响应信息
            if (!context.getIsStream() && response.getContent() != null && !response.getContent().isEmpty()) {
                printer.send("plan_thought", response.getContent());
            }

            // 记录响应信息
            log.info("{} {}'s thoughts: {}", context.getRequestId(), getName(), response.getContent());
            log.info("{} {} selected {} tools to use", context.getRequestId(), getName(),
                    response.getToolCalls() != null ? response.getToolCalls().size() : 0);

            // 创建并添加助手消息
            Message assistantMsg = response.getToolCalls() != null && !response.getToolCalls().isEmpty() && !"struct_parse".equals(llm.getFunctionCallType()) ?
                    Message.fromToolCalls(response.getContent(), response.getToolCalls()) :
                    Message.assistantMessage(response.getContent(), null);

            getMemory().addMessage(assistantMsg);

        } catch (Exception e) {

            log.error("{} think error ", context.getRequestId(), e);
        }

        return true;
    }

    @Override
    public String act() {
        // 关闭了动态更新Plan，直接执行下一个task
        if (isColseUpdate) {
            if (Objects.nonNull(planningTool.getPlan())) {
                return getNextTask();
            }
        }

        List<String> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (ToolCall toolCall : toolCalls) {
            String result = executeTool(toolCall);
            if (maxObserve != null) {
                result = result.substring(0, Math.min(result.length(), maxObserve));
            }
            results.add(result);

            // 添加工具响应到记忆
            if ("struct_parse".equals(llm.getFunctionCallType())) {
                String content = getMemory().getLastMessage().getContent();
                getMemory().getLastMessage().setContent(content + "\n 工具执行结果为:\n" + result);
            } else { // function_call
                Message toolMsg = Message.toolMessage(
                        result,
                        toolCall.getId(),
                        null
                );
                getMemory().addMessage(toolMsg);
            }
        }


        if (Objects.nonNull(planningTool.getPlan())) {
            if (isColseUpdate) {
                planningTool.stepPlan();
            }
            return getNextTask();
        }

        return String.join("\n\n", results);
    }


    private String getNextTask() {
        boolean allComplete = true;
        for (String status : planningTool.getPlan().getStepStatus()) {
            if (!"completed".equals(status)) {
                allComplete = false;
                break;
            }
        }

        if (allComplete) {
            setState(AgentState.FINISHED);
            printer.send("plan", planningTool.getPlan());
            return "finish";
        }

        if (!planningTool.getPlan().getCurrentStep().isEmpty()) {
            setState(AgentState.FINISHED);
            String[] currentSteps = planningTool.getPlan().getCurrentStep().split("<sep>");
            printer.send("plan", planningTool.getPlan());
            Arrays.stream(currentSteps).forEach(step -> printer.send("task", step));
            return planningTool.getPlan().getCurrentStep();
        }
        return "";
    }

    @Override
    public String run(String request) {
        if (Objects.isNull(planningTool.getPlan())) {
            GenieConfig genieConfig = SpringContextHolder.getApplicationContext().getBean(GenieConfig.class);
            request = genieConfig.getPlanPrePrompt() + request;
        }
        return super.run(request);
    }
}