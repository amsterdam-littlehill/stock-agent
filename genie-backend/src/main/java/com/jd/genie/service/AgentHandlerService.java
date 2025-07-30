package com.jd.genie.service;

import com.jd.genie.agent.agent.AgentContext;
import com.jd.genie.model.req.AgentRequest;

public interface AgentHandlerService {

    /**
     * 处理Agent请求
     */
    String handle(AgentContext context, AgentRequest request);

    /**
     * 进入handler条件
     */
    Boolean support(AgentContext context, AgentRequest request);

}