package com.dzenthai.cryptora.job;

import com.binance.api.client.exception.BinanceApiException;
import com.dzenthai.cryptora.service.AnalyticService;
import com.dzenthai.cryptora.service.FetchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@EnableScheduling
public class AppScheduler {

    private final AnalyticService analyticService;

    private final FetchService fetchService;

    public AppScheduler(AnalyticService analyticService, FetchService fetchService) {
        this.analyticService = analyticService;
        this.fetchService = fetchService;
    }

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

    @Scheduled(fixedRate = 10000)
    public void executeInSequence() {
        fetchNewQuotesAsync()
                .thenCompose(_ -> analyzeAndGenerateSignalsAsync())
                .exceptionally(ex -> {
                    log.error("Application Scheduler | Error executing operations: {}", ex.getMessage(), ex);

                    retryFetchNewQuotes(1);

                    throw new BinanceApiException("Application Scheduler | Critical error executing tasks", ex);
                });
    }

    private void retryFetchNewQuotes(int retryCount) {
        try {
            int delay = (int) Math.pow(2, retryCount) * 1000;
            Thread.sleep(delay);
            fetchService.fetchNewQuotes();
        } catch (Exception e) {
            log.error("AppScheduler | Error during retry attempt {}, exception: ", retryCount, e);
            if (retryCount < 5) {
                retryFetchNewQuotes(retryCount + 1);
            } else {
                log.error("AppScheduler | Max retry attempts reached. Aborting.");
            }
        }
    }
}
