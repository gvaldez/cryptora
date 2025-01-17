package com.dzenthai.cryptora.model.dto;

import lombok.Builder;
import java.math.BigDecimal;


@Builder
public record Average(
        BigDecimal openPrice,
        BigDecimal closePrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal tradePrice,
        BigDecimal priceRange
)
{}
