package com.dzenthai.cryptora.analyze.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class FetchService {

    private final QuoteService quoteService;

    private final BinanceApiRestClient binanceApiRestClient;

    @Transactional
    public void fetchNewQuotes() {
        log.debug("Fetch Service | Fetching new quotes");
        List<String> symbols = List.of("BTCUSDT", "ETHUSDT", "TONUSDT");
        LocalDateTime fetchTime = LocalDateTime.now();
        symbols.forEach(symbol -> {
            try {
                TickerPrice tickerPrice = binanceApiRestClient.getPrice(symbol);
                String ticker = tickerPrice.getSymbol();
                BigDecimal price = new BigDecimal(tickerPrice.getPrice());
                quoteService.save(ticker, price, fetchTime);
            } catch (BinanceApiException e) {
                log.error("Fetch Service | Binance API error while fetching, quotes: {}, exception: ", symbol, e);
            } catch (Exception e) {
                log.error("Fetch Service | Unexpected error while fetching, quotes: {}, exception: ", symbol, e);
            }
        });
    }

    @Transactional
    public void clearOldQuotes() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(10);
        try {
            quoteService.clear(cutoffTime);
            log.debug("Fetch Service | Quotes successfully cleared. Deletion time: {}", cutoffTime);
        } catch (BinanceApiException e) {
            log.error("Fetch Service | Error while clearing quotes. Deletion time: {}, exception: ", cutoffTime, e);
        }
    }
}
