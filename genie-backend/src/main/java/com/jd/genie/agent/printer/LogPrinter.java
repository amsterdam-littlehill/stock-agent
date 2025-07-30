package com.jd.genie.agent.printer;

import com.alibaba.fastjson.JSON;
import com.jd.genie.agent.enums.AgentType;
import com.jd.genie.model.req.AgentRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogPrinter implements Printer {
    private final AgentRequest request;

    public LogPrinter(AgentRequest request) {
        this.request = request;
    }

    @Override
    public void send(String messageId, String messageType, Object message, String digitalEmployee, Boolean isFinal) {
        if ("deep_search".equals(messageType)) {
            message = JSON.toJSONString(message);
        }
        log.info("{} {} {} {} {} {}", request.getRequestId(), messageId, messageType, message, digitalEmployee, isFinal);
    }

    @Override
    public void send(String messageType, Object message, String digitalEmployee) {
        send(null, messageType, message, digitalEmployee, true);
    }

    @Override
    public void send(String messageType, Object message) {
        send(null, messageType, message, null, true);
    }

    @Override
    public void send(String messageId, String messageType, Object message, Boolean isFinal) {
        send(messageId, messageType, message, null, isFinal);
    }

    @Override
    public void close() {
    }

    @Override
    public void updateAgentType(AgentType agentType) {
    }
}