package com.jd.genie.service.impl;

import com.jd.genie.agent.agent.AgentContext;
import com.jd.genie.agent.agent.ReActAgent;
import com.jd.genie.agent.agent.ReactImplAgent;
import com.jd.genie.agent.agent.SummaryAgent;
import com.jd.genie.agent.dto.File;
import com.jd.genie.agent.dto.TaskSummaryResult;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.config.GenieConfig;
import com.jd.genie.model.req.AgentRequest;
import com.jd.genie.service.AgentHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
public class ReactHandlerImpl implements AgentHandlerService {

    @Autowired
    private GenieConfig genieConfig;


    @Override
    public String handle(AgentContext agentContext, AgentRequest request) {

        ReActAgent executor = new ReactImplAgent(agentContext);
        SummaryAgent summary = new SummaryAgent(agentContext);
        summary.setSystemPrompt(summary.getSystemPrompt().replace("{{query}}", request.getQuery()));

        executor.run(request.getQuery());
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

        return "";
    }

    @Override
    public Boolean support(AgentContext agentContext, AgentRequest request) {
        return AgentType.REACT.getValue().equals(request.getAgentType());
    }
}
