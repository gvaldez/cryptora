package com.orbesource.cryptora.model.dto;


public record Statistic(
        String ticker,
        Average average,
        Total total,
        Info info
)
{}
