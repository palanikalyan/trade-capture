package com.tcmq.tradecapture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class MqSender {

    private final JmsTemplate jms;
    private final ObjectMapper mapper = new ObjectMapper();

    public MqSender(JmsTemplate jms) {
        this.jms = jms;
    }

    public void send(TradeMessage msg) {
        try {
            String json = mapper.writeValueAsString(msg);

            jms.convertAndSend("fraud_requests", json);
            jms.convertAndSend("rules_requests", json);

            System.out.println("Sent to fraud_requests & rules_requests → " + json);

        } catch (Exception e) {
            throw new RuntimeException("JSON conversion failed", e);
        }
    }
}
