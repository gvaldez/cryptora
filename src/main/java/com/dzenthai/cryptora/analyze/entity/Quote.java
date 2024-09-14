package com.dzenthai.cryptora.analyze.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@Document(collection = "quotes")
public class Quote {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field(name = "ticker")
    private String ticker;

    @Field(name = "price")
    private BigDecimal price;

    @Field(name = "time")
    private LocalDateTime time;

}
