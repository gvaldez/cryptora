package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;


@Slf4j
@Service
public class AIService {

    private final OllamaChatModel chatModel;

    private final QuoteService quoteService;

    public AIService(
            OllamaChatModel chatModel,
            QuoteService quoteService
    ) {
        this.chatModel = chatModel;
        this.quoteService = quoteService;
    }

    public String getAnalysis(String ticker) {
        return String.format(generateResponse(ticker));
    }

    public String generateResponse(String ticker) {
        return chatModel.call(
                        new Prompt(buildPrompt(ticker),
                                new OllamaOptions()
                                        .withModel(OllamaModel.LLAMA3_2_1B)
                                        .withTemperature(0.4)))
                .getResult()
                .getOutput()
                .getContent();
    }

    private List<Quote> getQuotesByTicker(String ticker) {
        return quoteService.getAllQuotes()
                .stream()
                .sorted(Comparator.comparing(Quote::getDatetime).reversed())
                .filter(quote -> quote.getTicker().equals(ticker.concat("USDT")))
                .toList();
    }

    private BigDecimal calculateAveragePrice(List<Quote> quotes) {
        BigDecimal sum = quotes.stream()
                .map(Quote::getClosePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMinPrice(List<Quote> quotes) {
        return quotes.stream()
                .map(Quote::getLowPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateMaxPrice(List<Quote> quotes) {
        return quotes.stream()
                .map(Quote::getHighPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    public String buildResult(String ticker) {
        List<Quote> quotes = getQuotesByTicker(ticker);

        if (quotes.isEmpty()) {
            log.warn("AIService | No data available for the ticker: {}", ticker);
        }

        BigDecimal averagePrice = calculateAveragePrice(quotes);
        BigDecimal minPrice = calculateMinPrice(quotes);
        BigDecimal maxPrice = calculateMaxPrice(quotes);

        return String.format("""
                Analyze cryptocurrency data for the period:
                            1. Price Analytics:
                               - Average price: %s
                               - Minimum price: %s
                               - Maximum price: %s
                            2. Recommendations:
                """, averagePrice, minPrice, maxPrice);
    }

    private String buildPrompt(String ticker) {
        return String.format(buildResult(ticker) + """
                [Short response]
                Analysis of why the action is recommended,
                including factors like market conditions,
                trends, and supporting data.
                """
        );
    }
}
