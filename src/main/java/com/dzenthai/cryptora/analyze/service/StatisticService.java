package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;


@Slf4j
@Service
public class StatisticService {

    private final QuoteService quoteService;

    public StatisticService(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    public String generateStatisticReport(String ticker) {

        List<Quote> quotes = quoteService.getQuotesByTicker(ticker);

        log.debug("StatisticService | Generate statistic report for the ticker: {}", ticker);

        if (quotes.isEmpty()) {
            String warn = String.format("The ticker with the name %s does not exist or information about it is unavailable.", ticker);
            log.warn("StatisticService | {}", warn);
            return warn;
        }

        return String.format("""
                        Statistic for: %s
                        1. Average:
                            - Open Price: %s
                            - Close Price: %s
                            - High Price: %s
                            - Low Price: %s
                            - Trade Price: %s
                            - Price Range: %s
                        2. Total:
                            - Volume: %s
                            - Amount: %s
                        """,
                ticker,
                calculateAverageOpenPrice(quotes),
                calculateAverageClosePrice(quotes),
                calculateAverageHighPrice(quotes),
                calculateAverageLowPrice(quotes),
                calculateAverageTradePrice(quotes),
                calculateAveragePriceRange(quotes),
                calculateTotalVolume(quotes),
                calculateTotalAmount(quotes));
    }

    private BigDecimal calculateAverageOpenPrice(List<Quote> quotes) {
        return calculateAverage(quotes, Quote::getOpenPrice);
    }

    private BigDecimal calculateAverageClosePrice(List<Quote> quotes) {
        return calculateAverage(quotes, Quote::getClosePrice);
    }

    private BigDecimal calculateAverageHighPrice(List<Quote> quotes) {
        return calculateAverage(quotes, Quote::getHighPrice);
    }

    private BigDecimal calculateAverageLowPrice(List<Quote> quotes) {
        return calculateAverage(quotes, Quote::getLowPrice);
    }

    private BigDecimal calculateAveragePriceRange(List<Quote> quotes) {
        return calculateAverage(quotes, quote ->
                quote.getHighPrice().subtract(quote.getLowPrice()));
    }

    private BigDecimal calculateTotalVolume(List<Quote> quotes) {
        return calculateTotal(quotes, Quote::getVolume);
    }

    private BigDecimal calculateTotalAmount(List<Quote> quotes) {
        return calculateTotal(quotes, Quote::getAmount);
    }

    private BigDecimal calculateAverageTradePrice(List<Quote> quotes) {
        BigDecimal totalAmount = calculateTotalAmount(quotes);
        BigDecimal totalVolume = calculateTotalVolume(quotes);
        return quotes.isEmpty() ? BigDecimal.ZERO : totalAmount.divide(totalVolume, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverage(List<Quote> quotes, Function<Quote, BigDecimal> function) {
        return quotes.isEmpty() ? BigDecimal.ZERO : quotes.stream()
                .map(function).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(quotes.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotal(List<Quote> quotes, Function<Quote, BigDecimal> function) {
        return quotes.isEmpty() ? BigDecimal.ZERO : quotes.stream()
                .map(function)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
