package com.tcmq.tradecapture;

import java.io.Serializable;

public class TradeMessage implements Serializable {
    public String tradeId;
    public double quantity;
    public double price;
}
