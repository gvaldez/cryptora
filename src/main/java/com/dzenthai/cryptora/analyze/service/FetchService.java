package com.dzenthai.cryptora.analyze.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.exception.BinanceApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class FetchService {

    private final QuoteService quoteService;

    private final BinanceApiRestClient binanceApiRestClient;

    public FetchService(QuoteService quoteService, BinanceApiRestClient binanceApiRestClient) {
        this.quoteService = quoteService;
        this.binanceApiRestClient = binanceApiRestClient;
    }

    @Transactional
    public void fetchNewQuotes() {
        log.debug("FetchService | Fetching new quotes");
        List<String> symbols = List.of("BTCUSDT", "ETHUSDT", "TONUSDT");
        symbols.forEach(symbol -> {
            try {
                var tickerPrice = binanceApiRestClient.getPrice(symbol);
                List<Candlestick> candlesticks = binanceApiRestClient.getCandlestickBars(symbol, CandlestickInterval.ONE_MINUTE);
                var quote = quoteService.addNewQuote(tickerPrice, candlesticks);
                log.debug("FetchService | Quote successfully saved, quote: {}", quote);
            } catch (BinanceApiException e) {
                log.error("FetchService | Binance API error while fetching, quotes: {}, exception: ", symbol, e);
            } catch (Exception e) {
                log.error("FetchService | Unexpected error while fetching, quotes: {}, exception: ", symbol, e);
            }
        });
    }
}
