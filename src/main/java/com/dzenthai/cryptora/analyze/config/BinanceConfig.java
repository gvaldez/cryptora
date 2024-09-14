package com.dzenthai.cryptora.analyze.config;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class BinanceConfig {

    @Value("${binance.api.key}")
    private String apiKey;

    @Value("${binance.api.secret}")
    private String apiSecret;

    @Bean
    public BinanceApiRestClient binanceApiRestClient() {
        return BinanceApiClientFactory
                .newInstance(apiKey, apiSecret)
                .newRestClient();
    }
}
