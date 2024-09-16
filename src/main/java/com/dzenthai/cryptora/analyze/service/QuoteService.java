package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import com.dzenthai.cryptora.analyze.repository.QuoteRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepo quoteRepo;

    @Transactional
    public List<Quote> getAllQuotes() {
        return quoteRepo.findAll();
    }

    @Transactional
    public void clear(LocalDateTime upTo) {
        log.debug("Quote Service | Clearing quotes older than: {}", upTo);
        quoteRepo.deleteAllByTimeLessThan(upTo);
    }

    @Transactional
    public void save(String ticker, BigDecimal price, LocalDateTime time) {
        log.debug("Quote Service | Saving quote, ticker: {}, price: {}, time: {}", ticker, price, time);
        Quote quote = Quote.builder()
                .ticker(ticker)
                .price(price)
                .time(time)
                .build();
        quoteRepo.save(quote);
        log.debug("Quote Service | Quote successfully saved: {}", quote);
    }
}
