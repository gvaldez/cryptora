package com.dzenthai.cryptora.controller;

import com.dzenthai.cryptora.service.OllamaService;
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

    private final OllamaService ollamaService;

    public ReportController(
            StatisticService statisticService,
            OllamaService ollamaService
    ) {
        this.statisticService = statisticService;
        this.ollamaService = ollamaService;
    }

    @GetMapping("/{ticker}")
    public Map<String, ?> getReport(@PathVariable String ticker) {
        return Map.of(
                "statistic", statisticService.calculateStatisticReport(ticker),
                "recommendation", ollamaService.generateAIResponse(ticker)
        );
    }
}
