package com.dzenthai.cryptora.analyze.controller;

import com.dzenthai.cryptora.analyze.service.AIService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/recommendation/{ticker}")
    public String getAIRecommendation(@PathVariable String ticker) {
        return aiService.getAIRecommendation(ticker);
    }
}
