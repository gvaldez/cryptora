package com.orbesource.cryptora.model.dto;

public record ClientOrder(String clientOrderId, String side, String price, String symbol, Boolean isWorking) {
}
