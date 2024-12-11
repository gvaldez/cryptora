package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Slf4j
@Service
public class StatisticService {

    private final QuoteService quoteService;

    public StatisticService(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    public String generateStatisticReport(String ticker) {
        List<Quote> quotes = quoteService.getQuotesByTicker(ticker);

        log.debug("AIService | Building result for the ticker: {}", ticker);

        if (quotes.isEmpty()) {
            String warn = String.format("The ticker with the name %s does not exist or information about it is unavailable.", ticker);
            log.warn("AIService | {}", warn);
            return warn;
        }

        return String.format("""
                Analysis for: %s
                    - Average Open Price: %s
                    - Average Close Price: %s
                    - Average High Price: %s
                    - Average Low Price: %s
                    - Total Volume: %s
                    - Total Amount: %s
                    - Average Trade Price: %s
                
                """,
                ticker,
                calculateAverageOpenPrice(quotes),
                calculateAverageClosePrice(quotes),
                calculateAverageHighPrice(quotes),
                calculateAverageLowPrice(quotes),
                calculateTotalVolume(quotes),
                calculateTotalAmount(quotes),
                calculateAverageTradePrice(quotes));
    }

    private BigDecimal calculateAverageOpenPrice(List<Quote> quotes) {
        BigDecimal averageOpenPrice = quotes.stream()
                .map(Quote::getOpenPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
        log.debug("AIService | Calculating average open price: {}", averageOpenPrice);
        return averageOpenPrice;
    }

    private BigDecimal calculateAverageClosePrice(List<Quote> quotes) {
        BigDecimal averageClosePrice = quotes.stream()
                .map(Quote::getClosePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
        log.debug("AIService | Calculating average close price: {}", averageClosePrice);
        return averageClosePrice;
    }

    private BigDecimal calculateAverageHighPrice(List<Quote> quotes) {
        BigDecimal averageHighPrice = quotes.stream()
                .map(Quote::getHighPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
        log.debug("AIService | Calculating average high price: {}", averageHighPrice);
        return averageHighPrice;
    }

    private BigDecimal calculateAverageLowPrice(List<Quote> quotes) {
        BigDecimal averageLowPrice = quotes.stream()
                .map(Quote::getLowPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
        log.debug("AIService | Calculating average low price: {}", averageLowPrice);
        return averageLowPrice;
    }

    private BigDecimal calculateTotalVolume(List<Quote> quotes) {
        BigDecimal totalVolume = quotes.stream()
                .map(Quote::getVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("AIService | Calculating total volume: {}", totalVolume);
        return totalVolume;
    }

    private BigDecimal calculateTotalAmount(List<Quote> quotes) {
        BigDecimal totalAmount = quotes.stream()
                .map(Quote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("AIService | Calculating total amount: {}", totalAmount);
        return totalAmount;
    }

    private BigDecimal calculateAverageTradePrice(List<Quote> quotes) {
        BigDecimal totalAmount = calculateTotalAmount(quotes);
        BigDecimal totalVolume = calculateTotalVolume(quotes);
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Total volume is zero, cannot calculate average trade price.");
        }
        BigDecimal averageTradePrice = totalAmount.divide(totalVolume, RoundingMode.HALF_UP);
        log.debug("AIService | Calculating average trade price: {}", averageTradePrice);
        return averageTradePrice;
    }
}
