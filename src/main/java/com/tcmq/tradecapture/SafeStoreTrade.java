package com.tcmq.tradecapture;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SafeStoreTrade {

    @Id
    public String tradeId;

    @Column(columnDefinition = "TEXT")
    public String payloadJson;

    // high-level processing state: RECEIVED, PERSISTED, PUBLISHED, NOTIFIED, FAILED
    public String status;

    @Column(columnDefinition = "TEXT")
    public String lastNotification;

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
