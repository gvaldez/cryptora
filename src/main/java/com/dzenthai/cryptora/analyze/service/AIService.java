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
        log.debug("AIService | Getting analysis for the ticker: {}", ticker);
        return buildResult(ticker) + generateAIResponse(ticker);
    }

    public String generateAIResponse(String ticker) {
        var prompt = buildPrompt(ticker);
        log.debug("AIService | Generating AI response for the ticker: {}, prompt: {}", ticker, prompt);
        return chatModel.call(
                        new Prompt(prompt,
                                new OllamaOptions()
                                        .withModel(OllamaModel.LLAMA3_2_1B)
                                        .withTemperature(0.4)))
                .getResult()
                .getOutput()
                .getContent();
    }

    private BigDecimal calculateAverageOpenPrice(List<Quote> quotes) {
        BigDecimal totalOpenPrice = BigDecimal.ZERO;
        for (Quote quote : quotes) {
            totalOpenPrice = totalOpenPrice.add(quote.getOpenPrice());
        }
        log.debug("AIService | Calculating average open price: {}", totalOpenPrice);
        return totalOpenPrice.divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageClosePrice(List<Quote> quotes) {
        BigDecimal totalClosePrice = BigDecimal.ZERO;
        for (Quote quote : quotes) {
            totalClosePrice = totalClosePrice.add(quote.getClosePrice());
        }
        log.debug("AIService | Calculating average close price: {}", totalClosePrice);
        return totalClosePrice.divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageHighPrice(List<Quote> quotes) {
        BigDecimal totalHighPrice = BigDecimal.ZERO;
        for (Quote quote : quotes) {
            totalHighPrice = totalHighPrice.add(quote.getHighPrice());
        }
        log.debug("AIService | Calculating average high price {}", totalHighPrice);
        return totalHighPrice.divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageLowPrice(List<Quote> quotes) {
        BigDecimal totalLowPrice = BigDecimal.ZERO;
        for (Quote quote : quotes) {
            totalLowPrice = totalLowPrice.add(quote.getLowPrice());
        }
        log.debug("AIService | Calculating average low price: {}", totalLowPrice);
        return totalLowPrice.divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalVolume(List<Quote> quotes) {
        BigDecimal totalVolume = BigDecimal.ZERO;
        for (Quote quote : quotes) {
            totalVolume = totalVolume.add(quote.getVolume());
        }
        log.debug("AIService | Calculating total volume: {}", totalVolume);
        return totalVolume;
    }

    private BigDecimal calculateTotalAmount(List<Quote> quotes) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Quote quote : quotes) {
            totalAmount = totalAmount.add(quote.getAmount());
        }
        log.debug("AIService | Calculating total amount: {}", totalAmount);
        return totalAmount;
    }

    private BigDecimal calculateAverageTradePrice(List<Quote> quotes) {
        BigDecimal averageTradePrice =  calculateTotalAmount(quotes)
                .divide(calculateTotalVolume(quotes), RoundingMode.HALF_UP);
        log.debug("AIService | Calculating average trade price: {}", averageTradePrice);
        return averageTradePrice;
    }

    private String buildResult(String ticker) {
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

    private String buildPrompt(String ticker) {
        log.debug("AIService | Building prompt for the ticker: {}", ticker);
        return String.format(buildResult(ticker) + """
                [Output the information below]
                Analysis of why the action is recommended,
                including factors like market conditions,
                trends, and supporting data.
                """
        );
    }
}
