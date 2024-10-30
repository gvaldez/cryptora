package com.dzenthai.cryptora.analyze.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import com.dzenthai.cryptora.analyze.entity.data.Ticker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class FetchService {

    private final QuoteService quoteService;

    private final BinanceApiRestClient binanceApiRestClient;

    public FetchService(QuoteService quoteService, BinanceApiRestClient binanceApiRestClient) {
        this.quoteService = quoteService;
        this.binanceApiRestClient = binanceApiRestClient;
    }

    public void fetchNewQuotes() {
        log.debug("FetchService | Fetching new quotes");
        List<String> symbols = Ticker.getAllSymbols();
        symbols.forEach(symbol -> {
            try {
                fetchTickerPrice(symbol).ifPresentOrElse(
                        tickerPrice -> fetchCandlestickBars(symbol).ifPresentOrElse(
                                candlesticks -> {
                                    if (candlesticks.isEmpty()) {
                                        return;
                                    }
                                    var quote = quoteService.addNewQuote(tickerPrice, candlesticks);
                                    log.debug("FetchService | Quote successfully saved, quote: {}", quote);
                                },
                                () -> log.warn("FetchService | Candlesticks are empty for symbol: {}", symbol)
                        ),
                        () -> log.warn("FetchService | Ticker price is null for symbol: {}", symbol)
                );
            } catch (BinanceApiException e) {
                log.error("FetchService | Binance API error while fetching, quotes: {}, exception: ", symbol, e);
            } catch (Exception e) {
                log.error("FetchService | Unexpected error while fetching, quotes: {}, exception: ", symbol, e);
            }
        });
    }


    private Optional<TickerPrice> fetchTickerPrice(String symbol) {
        return Optional.ofNullable(binanceApiRestClient.getPrice(symbol));
    }

    private Optional<List<Candlestick>> fetchCandlestickBars(String symbol) {
        return Optional.ofNullable(binanceApiRestClient.getCandlestickBars(symbol, CandlestickInterval.ONE_MINUTE));
    }
}