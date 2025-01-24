package com.orbesource.cryptora.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.TickerPrice;
import com.orbesource.cryptora.model.entity.Quote;
import com.orbesource.cryptora.repository.QuoteRepo;

@Service
public class QuoteService {

	private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);

	private final QuoteRepo quoteRepo;

	public QuoteService(QuoteRepo quoteRepo) {
		this.quoteRepo = quoteRepo;
	}

	@Transactional(readOnly = true)
	public List<Quote> getAllQuotes() 
	{
		logger.debug("QuoteService | Receiving all quotes");
		return (List<Quote>) quoteRepo.findAll();
	}

	@Transactional(readOnly = true)
	public List<Quote> getQuotesByTicker(String ticker) {
		logger.debug("QuoteService | Receiving all quotes by ticker: {}", ticker);
		return ((Collection<Quote>) quoteRepo.findAll()).stream().filter(quote -> quote.getTicker().equals(ticker.concat("USDT"))).toList();
	}

	@Transactional
	public Quote save(Quote quote) {
		logger.debug("QuoteService | Quote successfully saved, quote: {}", quote);
		return quoteRepo.save(quote);
	}

	@Transactional
	public Quote addNewQuote(TickerPrice tickerPrice, List<Candlestick> candlesticks) {

	    Candlestick candlestick = candlesticks.get(candlesticks.size() - 1);
		var quote = buildQuote(tickerPrice, candlestick);

		logger.debug("QuoteService | Adding new quote with ticker: {}, quote: {}, datetime: {}", tickerPrice, quote,
				quote.getDatetime());
		return save(quote);
	}

	private Quote buildQuote(TickerPrice tickerPrice, Candlestick candlestick) 
	{
		logger.debug("QuoteService | Building new quote, ticker: {}, candlestick: {}", tickerPrice, candlestick);

		Quote quote = new Quote();
		quote.setTicker(tickerPrice.getSymbol());
		quote.setOpenPrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getOpen())));
		quote.setHighPrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getHigh())));
		quote.setLowPrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getLow())));
		quote.setClosePrice(BigDecimal.valueOf(Double.parseDouble(candlestick.getClose())));
		quote.setVolume(BigDecimal.valueOf(Double.parseDouble(candlestick.getVolume())));
		quote.setAmount(BigDecimal.valueOf(Double.parseDouble(candlestick.getQuoteAssetVolume())));
		quote.setDatetime(LocalDateTime.now(ZoneOffset.UTC));
		return quote;
	}
}
