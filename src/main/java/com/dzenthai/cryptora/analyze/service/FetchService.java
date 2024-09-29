package com.dzenthai.cryptora.analyze.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        log.debug("Fetch Service | Fetching new quotes");
        List<String> symbols = List.of("BTCUSDT", "ETHUSDT", "TONUSDT");
        symbols.forEach(symbol -> {
            try {
                TickerPrice tickerPrice = binanceApiRestClient.getPrice(symbol);
                List<Candlestick> candlesticks = binanceApiRestClient.getCandlestickBars(symbol, CandlestickInterval.ONE_MINUTE);
                Candlestick candlestick = candlesticks.getLast();
                String ticker = tickerPrice.getSymbol();
                var openPrice = new BigDecimal(candlestick.getOpen());
                var highPrice = new BigDecimal(candlestick.getHigh());
                var lowPrice = new BigDecimal(candlestick.getLow());
                var closePrice = new BigDecimal(candlestick.getClose());
                var volume = new BigDecimal(candlestick.getVolume());
                var amount = new BigDecimal(candlestick.getQuoteAssetVolume());
                LocalDateTime fetchTime = LocalDateTime.now();
                quoteService.save(
                        ticker,
                        openPrice,
                        highPrice,
                        lowPrice,
                        closePrice,
                        volume,
                        amount,
                        fetchTime
                );
            } catch (BinanceApiException e) {
                log.error("Fetch Service | Binance API error while fetching, quotes: {}, exception: ", symbol, e);
            } catch (Exception e) {
                log.error("Fetch Service | Unexpected error while fetching, quotes: {}, exception: ", symbol, e);
            }
        });
    }
}
