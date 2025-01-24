package com.orbesource.cryptora.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.exception.BinanceApiException;
import com.orbesource.cryptora.model.enums.Ticker;


@Service
public class FetchService {

	private static final Logger logger = LoggerFactory.getLogger(FetchService.class);

    private final QuoteService quoteService;

    private final BinanceApiRestClient binanceApiRestClient;

    public FetchService(QuoteService quoteService,BinanceApiRestClient binanceApiRestClient) 
    {
        this.quoteService = quoteService;
        this.binanceApiRestClient = binanceApiRestClient;
    }

    public void fetchNewQuotes() 
    {
        logger.debug("FetchService | Fetching new quotes");
        List<String> symbols = Ticker.getAllSymbols();
        symbols.forEach(symbol -> 
        {
            logger.debug("FetchService | Processing symbol: {}", symbol);
            try 
            {
                TickerPrice tickerPrice = fetchTickerPrice(symbol);
                List<Candlestick> candlesticks = fetchCandlestickBars(symbol);

                var quote = quoteService.addNewQuote(tickerPrice, candlesticks);
                logger.debug("FetchService | Quote successfully saved, quote: {}", quote);

            } 
            catch (BinanceApiException e) 
            {
                logger.error("FetchService | Binance API error while fetching, symbol: {}, exception: ", symbol, e);
            } 
            catch (Exception e) 
            {
                logger.error("FetchService | Unexpected error while fetching, symbol: {}, exception: ", symbol, e);
            }
        });
    }

    private TickerPrice fetchTickerPrice(String symbol) 
    {
        TickerPrice tickerPrice = binanceApiRestClient.getPrice(symbol);
        if (tickerPrice == null) 
        {
            logger.warn("FetchService | Ticker Price is null for symbol: {}", symbol);
        }
        return tickerPrice;
    }

    private List<Candlestick> fetchCandlestickBars(String symbol) 
    {
        List<Candlestick> candlesticks = binanceApiRestClient.getCandlestickBars(symbol, CandlestickInterval.ONE_MINUTE);
        if (candlesticks == null || candlesticks.isEmpty()) 
        {
            logger.warn("FetchService | Candlesticks are empty for symbol: {}", symbol);
        }
        return candlesticks;
    }
}
