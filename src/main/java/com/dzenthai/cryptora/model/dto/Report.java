package com.dzenthai.cryptora.model.dto;

import lombok.Builder;


@Builder
public record Report(
        Statistic statistic,
        String recommendation
)
{}
