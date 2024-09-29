package com.dzenthai.cryptora.analyze.service;

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

    @Transactional
    public List<Quote> getAllQuotes() {
        return quoteRepo.findAll();
    }

    @Transactional
    public void save(String ticker, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal amount, BigDecimal volume, LocalDateTime time) {
        log.debug("Quote Service | Saving quote, ticker: {}, close price: {}, time: {}", ticker, close, time);
        Quote quote = Quote.builder()
                .ticker(ticker)
                .openPrice(open)
                .highPrice(high)
                .lowPrice(low)
                .closePrice(close)
                .amount(amount)
                .volume(volume)
                .datetime(time)
                .build();
        quoteRepo.save(quote);
        log.debug("Quote Service | Quote successfully saved: {}", quote);
    }
}
