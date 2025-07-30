package com.jd.genie.service;

import com.jd.genie.model.req.GptQueryReq;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IGptProcessService {

    /**
     * 单智能体，多智能体 Agent 增量接口.
     */
    SseEmitter queryMultiAgentIncrStream(GptQueryReq req);
}
