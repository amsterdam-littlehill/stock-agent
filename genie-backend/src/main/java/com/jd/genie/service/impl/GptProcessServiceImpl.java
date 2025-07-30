package com.jd.genie.service.impl;

import com.jd.genie.model.req.GptQueryReq;
import com.jd.genie.service.IGptProcessService;
import com.jd.genie.service.IMultiAgentService;
import com.jd.genie.util.ChateiUtils;
import com.jd.genie.util.SseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GptProcessServiceImpl implements IGptProcessService {
    @Autowired
    private IMultiAgentService multiAgentService;

    @Override
    public SseEmitter queryMultiAgentIncrStream(GptQueryReq req) {
        long timeoutMillis = TimeUnit.HOURS.toMillis(1);
        req.setUser("genie");
        req.setDeepThink(req.getDeepThink() == null ? 0: req.getDeepThink());
        String traceId = ChateiUtils.getRequestId(req);
        req.setTraceId(traceId);
        final SseEmitter emitter = SseUtil.build(timeoutMillis, req.getTraceId());
        multiAgentService.searchForAgentRequest(req, emitter);
        log.info("queryMultiAgentIncrStream GptQueryReq request:{}", req);
        return emitter;
    }
}
