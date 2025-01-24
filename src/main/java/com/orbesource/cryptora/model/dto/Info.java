package com.orbesource.cryptora.model.dto;

import java.time.LocalDateTime;
import java.util.Optional;

public record Info(
        Integer quoteEntriesCount,
        LocalDateTime currentDateTime,
        Optional<LocalDateTime> initDateTime
) {
}
