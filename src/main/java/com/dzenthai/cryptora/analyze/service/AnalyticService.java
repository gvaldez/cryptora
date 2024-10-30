package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import com.dzenthai.cryptora.analyze.facade.MessageSender;
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
    private final MessageSender messageSender;
    private final int shortTimePeriod;
    private final int longTimePeriod;

    public AnalyticService(QuoteService quoteService, MessageSender messageSender) {
        this.quoteService = quoteService;
        this.messageSender = messageSender;
        this.shortTimePeriod = 50;
        this.longTimePeriod = 200;
    }

    @Transactional
    public void analyzeAndGenerateSignals() {
        log.info("AnalyticService | Analysis started at {}", LocalDateTime.now());
        var quotesByTicker = groupQuotesByTicker();

        quotesByTicker.forEach(this::analyzeTickerQuotes);
    }

    private Map<String, List<Quote>> groupQuotesByTicker() {
        return quoteService.getAllQuotes().stream()
                .collect(Collectors.groupingBy(Quote::getTicker));
    }

    private void analyzeTickerQuotes(String ticker, List<Quote> quotes) {
        var shortCut = ticker.replaceAll("USDT", "");
        quotes.sort(Comparator.comparing(Quote::getDatetime));

        var series = buildBarSeries(quotes);
        if (series.getBarCount() < shortTimePeriod) {
            log.warn("AnalyticService | {}: Insufficient data for SMA calculation. Skipping...", shortCut);
            return;
        }

        evaluateSignals(series, shortCut);
    }

    private void evaluateSignals(BarSeries series, String shortCut) {
        SMAIndicator shortTermSMA = new SMAIndicator(new ClosePriceIndicator(series), shortTimePeriod);
        SMAIndicator longTermSMA = new SMAIndicator(new ClosePriceIndicator(series), longTimePeriod);

        Num latestPrice = series.getLastBar().getClosePrice();
        Num shortTermValue = shortTermSMA.getValue(series.getEndIndex());
        Num longTermValue = longTermSMA.getValue(series.getEndIndex());

        sendSignalMessage(latestPrice, shortTermValue, longTermValue, shortCut);
    }

    private void sendSignalMessage(Num latestPrice, Num shortTermValue, Num longTermValue, String shortCut) {
        var threshold = longTermValue.multipliedBy(DecimalNum.valueOf(1.01));

        if (shortTermValue.isGreaterThan(threshold) && latestPrice.isGreaterThan(shortTermValue)) {
            sendMessage("Buy", shortCut);
        } else if (shortTermValue.isLessThan(threshold) && latestPrice.isLessThan(shortTermValue)) {
            sendMessage("Sell", shortCut);
        } else {
            sendMessage("Hold", shortCut);
        }
    }

    private void sendMessage(String action, String shortCut) {
        log.info("AnalyticService | {}: {}", shortCut, action.toUpperCase());
        messageSender.send("%s %s".formatted(action, shortCut));
    }

    private BarSeries buildBarSeries(List<Quote> quotes) {
        var series = new BaseBarSeries();
        series.setMaximumBarCount(1000);

        ZonedDateTime lastBarEndTime = null;
        for (Quote quote : quotes) {
            ZonedDateTime endTime = quote.getDatetime().atZone(ZoneOffset.UTC);
            if (lastBarEndTime != null && !endTime.isAfter(lastBarEndTime)) {
                log.warn("AnalyticService | Bar with end time {} skipped; not later than previous {}",
                        endTime, lastBarEndTime);
                continue;
            }
            series.addBar(buildBar(endTime, quote));
            lastBarEndTime = endTime;
        }
        return series;
    }

    private Bar buildBar(ZonedDateTime endTime, Quote quote) {
        return new BaseBar(
                Duration.ofHours(1),
                endTime,
                DecimalNum.valueOf(quote.getOpenPrice()),
                DecimalNum.valueOf(quote.getHighPrice()),
                DecimalNum.valueOf(quote.getLowPrice()),
                DecimalNum.valueOf(quote.getClosePrice()),
                DecimalNum.valueOf(quote.getVolume()),
                DecimalNum.valueOf(quote.getAmount())
        );
    }
}
