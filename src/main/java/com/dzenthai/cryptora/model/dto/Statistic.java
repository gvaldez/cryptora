package com.dzenthai.cryptora.model.dto;

import lombok.Builder;

@Builder
public record Statistic(
        String ticker,
        Average average,
        Total total,
        Info info
)
{}
