package com.dzenthai.cryptora.model.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record Total(
        BigDecimal volume,
        BigDecimal amount
)
{}
