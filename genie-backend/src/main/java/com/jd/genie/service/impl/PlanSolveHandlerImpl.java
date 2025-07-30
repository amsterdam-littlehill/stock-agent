package com.jd.genie.service.impl;


import com.jd.genie.agent.agent.AgentContext;
import com.jd.genie.agent.agent.ExecutorAgent;
import com.jd.genie.agent.agent.PlanningAgent;
import com.jd.genie.agent.agent.SummaryAgent;
import com.jd.genie.agent.dto.File;
import com.jd.genie.agent.dto.TaskSummaryResult;
import com.jd.genie.agent.enums.AgentState;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.agent.util.ThreadUtil;
import com.jd.genie.config.GenieConfig;
import com.jd.genie.model.req.AgentRequest;
import com.jd.genie.service.AgentHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PlanSolveHandlerImpl implements AgentHandlerService {

    @Autowired
    private GenieConfig genieConfig;


    @Override
    public String handle(AgentContext agentContext, AgentRequest request) {

        PlanningAgent planning = new PlanningAgent(agentContext);
        ExecutorAgent executor = new ExecutorAgent(agentContext);
        SummaryAgent summary = new SummaryAgent(agentContext);
        summary.setSystemPrompt(summary.getSystemPrompt().replace("{{query}}", request.getQuery()));

        String planningResult = planning.run(agentContext.getQuery());
        int stepIdx = 0;
        int maxStepNum = genieConfig.getPlannerMaxSteps();
        while (stepIdx <= maxStepNum) {
            List<String> planningResults = Arrays.stream(planningResult.split("<sep>"))
                    .map(task -> "你的任务是：" + task)
                    .collect(Collectors.toList());
            String executorResult;
            agentContext.getTaskProductFiles().clear();
            if (planningResults.size() == 1) {
                executorResult = executor.run(planningResults.get(0));
            } else {
                Map<String, String> tmpTaskResult = new ConcurrentHashMap<>();
                CountDownLatch taskCount = ThreadUtil.getCountDownLatch(planningResults.size());
                int memoryIndex = executor.getMemory().size();
                List<ExecutorAgent> slaveExecutors = new ArrayList<>();
                for (String task : planningResults) {
                    ExecutorAgent slaveExecutor = new ExecutorAgent(agentContext);
                    slaveExecutor.setState(executor.getState());
                    slaveExecutor.getMemory().addMessages(executor.getMemory().getMessages());
                    slaveExecutors.add(slaveExecutor);
                    ThreadUtil.execute(() -> {
                        String taskResult = slaveExecutor.run(task);
                        tmpTaskResult.put(task, taskResult);
                        taskCount.countDown();
                    });
                }
                ThreadUtil.await(taskCount);
                for (ExecutorAgent slaveExecutor : slaveExecutors) {
                    for (int i = memoryIndex; i < slaveExecutor.getMemory().size(); i++) {
                        executor.getMemory().addMessage(slaveExecutor.getMemory().get(i));
                    }
                    slaveExecutor.getMemory().clear();
                    executor.setState(slaveExecutor.getState());
                }
                executorResult = String.join("\n", tmpTaskResult.values());
            }
            planningResult = planning.run(executorResult);
            if ("finish".equals(planningResult)) {
                //任务成功结束，总结任务
                TaskSummaryResult result = summary.summaryTaskResult(executor.getMemory().getMessages(), request.getQuery());

                Map<String, Object> taskResult = new HashMap<>();
                taskResult.put("taskSummary", result.getTaskSummary());

                if (CollectionUtils.isEmpty(result.getFiles())) {
                    if (!CollectionUtils.isEmpty(agentContext.getProductFiles())) {
                        List<File> fileResponses = agentContext.getProductFiles();
                        // 过滤中间搜索结果文件
                        fileResponses.removeIf(file -> Objects.nonNull(file) && file.getIsInternalFile());
                        Collections.reverse(fileResponses);
                        taskResult.put("fileList", fileResponses);
                    }
                } else {
                    taskResult.put("fileList", result.getFiles());
                }

                agentContext.getPrinter().send("result", taskResult);


                break;
            }
            if (planning.getState() == AgentState.IDLE || executor.getState() == AgentState.IDLE) {
                agentContext.getPrinter().send("result", "达到最大迭代次数，任务终止。");
                break;
            }
            if (planning.getState() == AgentState.ERROR || executor.getState() == AgentState.ERROR) {
                agentContext.getPrinter().send("result", "任务执行异常，请联系管理员，任务终止。");
                break;
            }
            stepIdx++;
        }

        return "";
    }

    @Override
    public Boolean support(AgentContext agentContext, AgentRequest request) {
        return AgentType.PLAN_SOLVE.getValue().equals(request.getAgentType());
    }
}
