package com.tcmq.tradecapture;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SafeStoreRepo extends JpaRepository<SafeStoreTrade, String> {
}
