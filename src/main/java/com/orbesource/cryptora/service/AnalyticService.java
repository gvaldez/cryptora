package com.orbesource.cryptora.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import com.orbesource.cryptora.model.entity.Quote;


@Service
public class AnalyticService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticService.class);
    private static final String BASE_CURRENCY = "USDT";
    private static final int MAX_BARS = 1000;

    @Value("${cryptora.short.time.period}")
    private Integer shortTimePeriod;

    @Value("${cryptora.long.time.period}")
    private Integer longTimePeriod;

    @Value("${cryptora.atr.period}")
    private Integer atrPeriod;

    @Value("${cryptora.atr.multiplier}")
    private Double atrMultiplier;

    private final QuoteService quoteService;

    public AnalyticService(QuoteService quoteService) 
    {
        this.quoteService = quoteService;
    }

    public void analyzeAndGenerateSignals() 
    {
        logger.info("AnalyticService | Analysis started at {}", LocalDateTime.now());
        var quotesByTicker = groupQuotesByTicker();
        quotesByTicker.forEach(this::analyzeTickerQuotes);
    }

    private Map<String, List<Quote>> groupQuotesByTicker() 
    {
        return quoteService.getAllQuotes()
                .stream()
                .collect(Collectors.groupingBy(Quote::getTicker));
    }

    private void analyzeTickerQuotes(String ticker, List<Quote> quotes) 
    {
        String shortCut = ticker.replace(BASE_CURRENCY, "");
        quotes.sort(Comparator.comparing(Quote::getDatetime));

        BarSeries series = buildBarSeries(quotes);

        if (series.getBarCount() < shortTimePeriod) 
        {
            logger.warn("AnalyticService | {}: Insufficient data for SMA calculation. Skipping...", shortCut);
            return;
        }

        evaluateSignals(series, shortCut);
    }

    private void evaluateSignals(BarSeries series, String shortCut) 
    {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);

        SMAIndicator shortTermSMA = new SMAIndicator(closePriceIndicator, shortTimePeriod);
        SMAIndicator longTermSMA = new SMAIndicator(closePriceIndicator, longTimePeriod);

        int endIndex = series.getEndIndex();
        Num latestPrice = series.getLastBar().getClosePrice();
        Num shortTermValue = shortTermSMA.getValue(endIndex);
        Num longTermValue = longTermSMA.getValue(endIndex);

        var thresholds = calculateThresholds(series, longTermValue);
        sendSignalMessage(latestPrice, shortTermValue, thresholds[0], thresholds[1], shortCut);
    }

    private Num[] calculateThresholds(BarSeries series, Num longTermValue) 
    {
        ATRIndicator atrIndicator = new ATRIndicator(series, atrPeriod);
        Num atrValue = atrIndicator.getValue(series.getEndIndex());

        Num thresholdUpper = longTermValue.plus(atrValue.multipliedBy(DecimalNum.valueOf(atrMultiplier)));
        Num thresholdLower = longTermValue.minus(atrValue.multipliedBy(DecimalNum.valueOf(atrMultiplier)));

        return new Num[]{thresholdUpper, thresholdLower};
    }

    private void sendSignalMessage(Num latestPrice, Num shortTermValue, Num thresholdUpper, Num thresholdLower, String shortCut) 
    {
        String action;

        if (shortTermValue.isGreaterThan(thresholdUpper) && latestPrice.isGreaterThan(shortTermValue)) 
        {
            action = "Buy";
        } 
        else if (shortTermValue.isLessThan(thresholdLower) && latestPrice.isLessThan(shortTermValue)) 
        {
            action = "Sell";
        } 
        else 
        {
            action = "Hold";
        }

        sendSignals(action, shortCut);
    }

    private void sendSignals(String action, String shortCut) 
    {
    	logger.info("AnalyticService | {}: {}", shortCut, action.toUpperCase());
    }

    private BarSeries buildBarSeries(List<Quote> quotes) 
    {
        BarSeries series = new BaseBarSeries();
        series.setMaximumBarCount(MAX_BARS);

        ZonedDateTime lastBarEndTime = null;
        for (Quote quote : quotes) 
        {
            ZonedDateTime endTime = quote.getDatetime().atZone(ZoneOffset.UTC);
            if (lastBarEndTime != null && !endTime.isAfter(lastBarEndTime)) 
            {
                logger.warn("AnalyticService | Bar with end time {} skipped; not later than previous {}", endTime, lastBarEndTime);
                continue;
            }
            series.addBar(buildBar(endTime, quote));
            lastBarEndTime = endTime;
        }

        return series;
    }

    private Bar buildBar(ZonedDateTime endTime, Quote quote) 
    {
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
