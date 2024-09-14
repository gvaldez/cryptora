package com.dzenthai.cryptora.analyze.job;

import com.binance.api.client.exception.BinanceApiException;
import com.dzenthai.cryptora.analyze.service.AnalyticService;
import com.dzenthai.cryptora.analyze.service.FetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class AppScheduler {

    private final AnalyticService analyticService;

    private final FetchService fetchService;

    @Async
    public CompletableFuture<Void> fetchNewQuotesAsync() {
        fetchService.fetchNewQuotes();
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> analyzeAndGenerateSignalsAsync() {
        analyticService.analyzeAndGenerateSignals();
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> clearOldQuotesAsync() {
        fetchService.clearOldQuotes();
        return CompletableFuture.completedFuture(null);
    }

    @Scheduled(fixedRate = 10000)
    public void executeInSequence() {
        fetchNewQuotesAsync()
                .thenCompose(analyze -> analyzeAndGenerateSignalsAsync())
                .thenCompose(clear -> clearOldQuotesAsync())
                .exceptionally(ex -> {
                    log.error("Application Scheduler | Ошибка при выполнении операций: {}", ex.getMessage(), ex);

                    retryFetchNewQuotes();

                    throw new BinanceApiException("Application Scheduler | Критическая ошибка при выполнении задач", ex);
                });
    }

    private void retryFetchNewQuotes() {
        try {
            Thread.sleep(5000);
            fetchService.fetchNewQuotes();
        } catch (InterruptedException e) {
            log.error("Application Scheduler | Ошибка при повторной попытке получения новых котировок: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

}
