package com.tcmq.tradecapture;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/trades")
public class IntakeController {

    private final SafeStoreRepo repo;
    private final MqSender sender;
    private final ObjectMapper mapper = new ObjectMapper();

    public IntakeController(SafeStoreRepo repo, MqSender sender) {
        this.repo = repo;
        this.sender = sender;
    }

    @Transactional
    @PostMapping
    public String acceptTrade(@RequestBody TradeMessage msg) throws Exception {

        SafeStoreTrade t = new SafeStoreTrade();
        t.tradeId = msg.tradeId;
        t.payloadJson = mapper.writeValueAsString(msg);
        t.status = "RECEIVED";
        t.createdAt = LocalDateTime.now();
        t.updatedAt = LocalDateTime.now();
        repo.save(t);

        // after persisting mark as PERSISTED
        t.status = "PERSISTED";
        t.updatedAt = LocalDateTime.now();
        repo.save(t);

        // send JMS messages with correlation headers
        sender.send(msg);

        // after successful send mark as PUBLISHED
        t.status = "PUBLISHED";
        t.updatedAt = LocalDateTime.now();
        repo.save(t);

        return "RECEIVED: " + msg.tradeId;
    }
}
