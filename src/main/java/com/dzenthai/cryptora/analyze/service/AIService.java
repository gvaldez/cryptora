package com.dzenthai.cryptora.analyze.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
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

    public String generateAIResponse(String ticker) {
        var prompt = generateAIPrompt(ticker);
        log.debug("AIService | Generating AI response for the ticker: {}, prompt: {}", ticker, prompt);
        return chatModel.call(new Prompt(prompt))
                .getResult()
                .getOutput()
                .getContent();
    }

    private String generateAIPrompt(String ticker) {
        log.debug("AIService | Generate AI prompt for the ticker: {}", ticker);
        var report = statisticService.generateStatisticReport(ticker);

        return String.format("""
                Provide a recommendation for %s,
                in the form of [Buy/Hold/Sell] based on the provided data:
                %s
                """, ticker, report);
    }
}
