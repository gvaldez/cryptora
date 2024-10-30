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
        if (candlesticks.isEmpty()) {
            log.warn("QuoteService | Candlesticks list is empty for ticker: {}", tickerPrice.getSymbol());
            return null;
        }
        log.debug("QuoteService | Adding new quote with ticker: {}", tickerPrice);
        var candlestick = candlesticks.getLast();
        var quote = Quote.builder()
                .ticker(tickerPrice.getSymbol())
                .openPrice(new BigDecimal(candlestick.getOpen()))
                .highPrice(new BigDecimal(candlestick.getHigh()))
                .lowPrice(new BigDecimal(candlestick.getLow()))
                .closePrice(new BigDecimal(candlestick.getClose()))
                .volume(new BigDecimal(candlestick.getVolume()))
                .amount(new BigDecimal(candlestick.getQuoteAssetVolume()))
                .datetime(LocalDateTime.now())
                .build();
        return save(quote);
    }
}
