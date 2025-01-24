package com.orbesource.cryptora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;


@Service
public class OllamaService {

	private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);

    private final OllamaChatModel chatModel;

    private final StatisticService statisticService;

    public OllamaService(OllamaChatModel chatModel,StatisticService statisticService) 
    {
        this.chatModel = chatModel;
        this.statisticService = statisticService;
    }

    public String generateAIResponse(String ticker) 
    {
        var prompt = generateAIPrompt(ticker);
        logger.debug("AIService | Generating AI response for the ticker: {}, prompt: {}", ticker, prompt);
        return chatModel.call(new Prompt(prompt))
                .getResult()
                .getOutput()
                .getContent();
    }

    private String generateAIPrompt(String ticker) 
    {
        logger.debug("AIService | Generate AI prompt for the ticker: {}", ticker);
        var report = statisticService.calculateStatisticReport(ticker);

        return String.format("""
                Provide a recommendation for %s,
                in the form of [Buy/Hold/Sell] based on the provided data:
                %s
                """, ticker, report);
    }
}
