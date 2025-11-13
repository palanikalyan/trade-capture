package com.tcmq.tradecapture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

import java.time.LocalDateTime;

@Component
public class NotificationListener {

    private final SafeStoreRepo repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public NotificationListener(SafeStoreRepo repo) {
        this.repo = repo;
    }

    @JmsListener(destination = "notifications")
    public void onMessage(Message message) {
        try {
            String json = null;
            if (message instanceof TextMessage tm) {
                json = tm.getText();
            } else {
                json = message.getBody(String.class);
            }

            String correlationId = null;
            try { correlationId = message.getStringProperty("correlationId"); } catch (JMSException ignored) {}

            if (correlationId == null || correlationId.isBlank()) {
                // try to extract tradeId from payload
                try {
                    var node = mapper.readTree(json);
                    correlationId = node.path("tradeId").asText(null);
                } catch (Exception ignored) {}
            }

            // debug: log raw payload and extracted correlation id
            System.out.println("NotificationListener received payload: " + json);
            System.out.println("NotificationListener extracted correlationId: '" + correlationId + "'");

            if (correlationId != null) {
                var opt = repo.findById(correlationId);
                if (opt.isPresent()) {
                    SafeStoreTrade t = opt.get();
                    t.status = "NOTIFIED";
                    t.lastNotification = json;
                    t.updatedAt = LocalDateTime.now();
                    repo.save(t);
                    System.out.println("Updated trade " + correlationId + " to NOTIFIED");
                    return;
                }
            }

            System.out.println("Received notification (no matching trade found): " + json);

        } catch (JMSException jmse) {
            System.err.println("JMS error in NotificationListener: " + jmse.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to handle notification: " + e.getMessage());
        }
    }
}
