package com.jd.genie.service;

import com.jd.genie.model.dto.AutoBotsResult;
import com.jd.genie.model.req.GptQueryReq;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IMultiAgentService {
    /**
     * 请求多 agent发送请求入口函数.
     * @param gptQueryReq
     * @param sseEmitter
     * @return
     */
    AutoBotsResult searchForAgentRequest(GptQueryReq gptQueryReq, SseEmitter sseEmitter);
}
