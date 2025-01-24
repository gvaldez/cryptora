package com.orbesource.cryptora.model.dto;

import java.math.BigDecimal;


public record Average(
        BigDecimal openPrice,
        BigDecimal closePrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal tradePrice,
        BigDecimal priceRange
)
{}
