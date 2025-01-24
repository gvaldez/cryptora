package com.orbesource.cryptora.model.dto;

import java.math.BigDecimal;

public record Total(
        BigDecimal volume,
        BigDecimal amount
)
{}
