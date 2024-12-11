package com.dzenthai.cryptora.analyze.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AIService {

    private final OllamaChatModel chatModel;

    private final StatisticService statisticService;

    public AIService(
            OllamaChatModel chatModel,
            StatisticService statisticService
    ) {
        this.chatModel = chatModel;
        this.statisticService = statisticService;
    }

    public String getAIRecommendation(String ticker) {
        log.debug("AIService | Getting analysis for the ticker: {}", ticker);
        return statisticService.generateStatisticReport(ticker) + generateAIResponse(ticker);
    }

    private String generateAIResponse(String ticker) {
        var prompt = generateAIPrompt(ticker);
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

    private String generateAIPrompt(String ticker) {
        log.debug("AIService | Building prompt for the ticker: {}", ticker);
        return statisticService.generateStatisticReport(ticker) + """
                Analyze the given cryptocurrency and provide a data-driven recommendation:
                    - Summarize market trends (price, volume, volatility).
                    - Identify key value drivers (news, sentiment, technology).
                    - Evaluate risks and opportunities (historical trends, recent updates).
                    - Conclude with a [Buy/Hold/Sell] recommendation supported by data.
                """;
    }
}
