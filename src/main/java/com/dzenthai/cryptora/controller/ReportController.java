package com.dzenthai.cryptora.controller;

import com.dzenthai.cryptora.service.AIService;
import com.dzenthai.cryptora.service.StatisticService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    private final StatisticService statisticService;

    private final AIService aiService;

    public ReportController(
            StatisticService statisticService,
            AIService aiService
    ) {
        this.statisticService = statisticService;
        this.aiService = aiService;
    }

    @GetMapping("/{ticker}")
    public Map<String, ?> getReport(@PathVariable String ticker) {
        return Map.of(
                "statistic", statisticService.generateStatisticReport(ticker),
                "recommendation", aiService.generateAIResponse(ticker)
        );
    }
}
