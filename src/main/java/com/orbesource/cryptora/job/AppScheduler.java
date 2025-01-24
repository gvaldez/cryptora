package com.orbesource.cryptora.job;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.binance.api.client.exception.BinanceApiException;
import com.orbesource.cryptora.service.AnalyticService;
import com.orbesource.cryptora.service.FetchService;


@Component
@EnableScheduling
public class AppScheduler {

	private static final Logger logger = LoggerFactory.getLogger(AppScheduler.class);

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
    public void executeInSequence() 
    {
    	fetchNewQuotesAsync()
        .thenCompose(result -> analyzeAndGenerateSignalsAsync())
        .exceptionally(ex -> {
            logger.error("Application Scheduler | Error executing operations: {}", ex.getMessage(), ex);

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
        	logger.error("AppScheduler | Error during retry attempt {}, exception: ", retryCount, e);
            if (retryCount < 5) {
                retryFetchNewQuotes(retryCount + 1);
            } else {
            	logger.error("AppScheduler | Max retry attempts reached. Aborting.");
            }
        }
    }
}
