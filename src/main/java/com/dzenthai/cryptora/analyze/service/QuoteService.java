package com.dzenthai.cryptora.analyze.service;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.TickerPrice;
import com.dzenthai.cryptora.analyze.entity.Quote;
import com.dzenthai.cryptora.analyze.repository.QuoteRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


@Slf4j
@Service
public class QuoteService {

    private final QuoteRepo quoteRepo;

    public QuoteService(QuoteRepo quoteRepo) {
        this.quoteRepo = quoteRepo;
    }

    @Transactional(readOnly = true)
    public List<Quote> getAllQuotes() {
        log.debug("QuoteService | Receiving all quotes");
        return quoteRepo.findAll();
    }

    @Transactional
    public Quote save(Quote quote) {
        log.debug("QuoteService | Quote successfully saved, quote: {}", quote);
        return quoteRepo.save(quote);
    }

    @Transactional
    public Quote addNewQuote(TickerPrice tickerPrice, List<Candlestick> candlesticks) {

        var candlestick = getLastCandlestick(candlesticks);
        var datetime = getCurrentUtcDateTime();
        var quote = buildQuote(tickerPrice, candlestick, datetime);

        log.debug("QuoteService | Adding new quote with ticker: {}, quote: {}", tickerPrice, quote);
        return save(quote);
    }

    private Candlestick getLastCandlestick(List<Candlestick> candlesticks) {
        return candlesticks.getLast();
    }

    private LocalDateTime getCurrentUtcDateTime() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private Quote buildQuote(TickerPrice tickerPrice, Candlestick candlestick, LocalDateTime datetime) {
        log.debug("QuoteService | Building new quote, ticker: {}, candlestick: {}, datetime: {}", tickerPrice, candlestick, datetime);
        return Quote.builder()
                .ticker(tickerPrice.getSymbol())
                .openPrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getOpen())))
                .highPrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getHigh())))
                .lowPrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getLow())))
                .closePrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getClose())))
                .volume(BigDecimal.valueOf(Double.parseDouble(candlestick.getVolume())))
                .amount(BigDecimal.valueOf(Double.parseDouble(candlestick.getQuoteAssetVolume())))
                .datetime(datetime)
                .build();
    }

}
