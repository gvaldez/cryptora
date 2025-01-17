package com.dzenthai.cryptora.model.dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
public record Info(
        Integer quoteEntriesCount,
        LocalDateTime currentDateTime,
        Optional<LocalDateTime> initDateTime
) {
}
