package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import lombok.extern.slf4j.Slf4j;
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
public class AnalyticService {

    private final QuoteService quoteService;

    private final int shortTimePeriod = 30;

    private final int longTimePeriod = 100;

    public AnalyticService(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Transactional
    public void analyzeAndGenerateSignals() {

        log.info("Analytic Service | Datetime: {}", LocalDateTime.now());

        Map<String, List<Quote>> quotesByTicker = quoteService.getAllQuotes().stream()
                .collect(Collectors.groupingBy(Quote::getTicker));

        for (Map.Entry<String, List<Quote>> entry : quotesByTicker.entrySet()) {
            String ticker = entry.getKey();

            String shortCut = ticker.replaceAll("USDT", "");

            List<Quote> tickerQuotes = entry.getValue();

            tickerQuotes.sort(Comparator.comparing(Quote::getDatetime));

            BarSeries series = buildBarSeries(tickerQuotes);

            log.debug("Analytic Service | {}: Bar count: {}", shortCut, series.getBarCount());

            if (series.getBarCount() < shortTimePeriod) {
                log.warn("Analytic Service | {}: Not enough bars to calculate SMA. Skipping...", shortCut);
                continue;
            }

            log.debug("Analytic Service | First bar: {}", series.getFirstBar());
            log.debug("Analytic Service | Last bar: {}", series.getLastBar());

            SMAIndicator shortTermSMA = new SMAIndicator(new ClosePriceIndicator(series), shortTimePeriod);
            SMAIndicator longTermSMA = new SMAIndicator(new ClosePriceIndicator(series), longTimePeriod);

            Num latestPrice = series.getLastBar().getClosePrice();
            Num shortTermValue = shortTermSMA.getValue(series.getEndIndex());
            Num longTermValue = longTermSMA.getValue(series.getEndIndex());

            if (shortTermValue.isGreaterThan(longTermValue) && latestPrice.isGreaterThan(shortTermValue)) {
                log.info("Analytic Service | {}: BUY", shortCut);
            } else if (shortTermValue.isLessThan(longTermValue) && latestPrice.isLessThan(shortTermValue)) {
                log.info("Analytic Service | {}: SELL", shortCut);
            } else {
                log.info("Analytic Service | {}: HOLD", shortCut);
            }
        }
    }

    private BarSeries buildBarSeries(List<Quote> quotes) {
        BarSeries series = new BaseBarSeries();

        series.setMaximumBarCount(Math.max(longTimePeriod, shortTimePeriod));

        ZonedDateTime lastBarEndTime = null;

        for (Quote quote : quotes) {

            ZonedDateTime endTime = quote.getDatetime().atZone(ZoneOffset.UTC);

            if (lastBarEndTime != null) {
                log.debug("Analytic Service | Current bar end time: {}, Last bar time: {}",
                        endTime, lastBarEndTime);
            }

            if (lastBarEndTime != null && !endTime.isAfter(lastBarEndTime)) {
                log.warn("Analytic Service | The bar with end time {} cannot be added because its time is less than or equal to the time of the last bar {}",
                        endTime, lastBarEndTime);
                continue;
            }

            Bar bar = new BaseBar(
                    Duration.ofMinutes(15),
                    endTime,
                    DecimalNum.valueOf(quote.getOpenPrice()),
                    DecimalNum.valueOf(quote.getHighPrice()),
                    DecimalNum.valueOf(quote.getLowPrice()),
                    DecimalNum.valueOf(quote.getClosePrice()),
                    DecimalNum.valueOf(quote.getVolume()),
                    DecimalNum.valueOf(quote.getAmount())
            );
            series.addBar(bar);

            lastBarEndTime = endTime;

            log.debug("Analytic Service | Bar added: {}", bar);
        }
        return series;
    }
}
