package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class AnalyticService {

    private final QuoteService quoteService;

    private final RedisService redisService;

    @Transactional
    public void analyzeAndGenerateSignals() {

        log.info("Analytic Service | Время: {}", LocalDateTime.now());

        List<Quote> quotes = quoteService.getAllQuotes();
        redisService.saveQuotesToRedis(quotes);

        List<Quote> redisQuotes = redisService.getQuotesFromRedis();

        Map<String, List<Quote>> quotesByTicker = redisQuotes.stream()
                .collect(Collectors.groupingBy(Quote::getTicker));

        for (Map.Entry<String, List<Quote>> entry : quotesByTicker.entrySet()) {
            String ticker = entry.getKey();
            List<Quote> tickerQuotes = entry.getValue();

            tickerQuotes.sort(Comparator.comparing(Quote::getTime));

            BarSeries series = buildBarSeries(tickerQuotes);

            SMAIndicator shortTermSMA = new SMAIndicator(new ClosePriceIndicator(series), 100);
            SMAIndicator longTermSMA = new SMAIndicator(new ClosePriceIndicator(series), 200);

            Num latestPrice = series.getLastBar().getClosePrice();

            Num shortTermValue = shortTermSMA.getValue(series.getEndIndex());
            Num longTermValue = longTermSMA.getValue(series.getEndIndex());

            String shortCut = ticker.replaceAll("USDT", "");

            if (shortTermValue.isGreaterThan(longTermValue) && latestPrice.isGreaterThan(shortTermValue)) {
                log.info("Analytic Service | Сигнал для {}: КУПИТЬ", shortCut);
            } else if (shortTermValue.isLessThan(longTermValue) && latestPrice.isLessThan(shortTermValue)) {
                log.info("Analytic Service | Сигнал для {}: ПРОДАТЬ", shortCut);
            } else {
                log.info("Analytic Service | Сигнал для {}: ПОДОЖДАТЬ", shortCut);
            }
        }
        redisService.deleteQuotesFromRedis();
    }

    private BarSeries buildBarSeries(List<Quote> quotes) {
        BarSeries series = new BaseBarSeries();
        ZonedDateTime lastBarEndTime = null;

        for (Quote quote : quotes) {
            ZonedDateTime endTime = quote.getTime().atZone(ZoneOffset.UTC);

            if (lastBarEndTime != null) {
                log.debug("Analytic Service | Текущее время окончания бара: {}, Время последнего бара: {}",
                        endTime, lastBarEndTime);
            }

            if (lastBarEndTime != null && !endTime.isAfter(lastBarEndTime)) {
                log.warn("Analytic Service | Бар с временем окончания {} не может быть добавлен, так как его время меньше или равно времени последнего бара {}",
                        endTime, lastBarEndTime);
                continue;
            }

            Bar bar = new BaseBar(
                    Duration.ofSeconds(10),
                    endTime,
                    DecimalNum.valueOf(quote.getPrice()),
                    DecimalNum.valueOf(quote.getPrice()),
                    DecimalNum.valueOf(quote.getPrice()),
                    DecimalNum.valueOf(quote.getPrice()),
                    DecimalNum.valueOf(1),
                    DecimalNum.valueOf(1)
            );
            series.addBar(bar);

            lastBarEndTime = endTime;

            log.debug("Analytic Service | Добавлен бар: {}", bar);
        }
        return series;
    }
}
