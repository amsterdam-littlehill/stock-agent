package com.jd.genie.handler;

import com.jd.genie.model.multi.EventResult;
import com.jd.genie.model.req.AgentRequest;
import com.jd.genie.model.response.AgentResponse;
import com.jd.genie.model.response.GptProcessResult;

import java.util.List;

public interface AgentResponseHandler {
    GptProcessResult handle(AgentRequest request,
                AgentResponse response,
                List<AgentResponse> agentRespList,
                EventResult eventResult);
}
