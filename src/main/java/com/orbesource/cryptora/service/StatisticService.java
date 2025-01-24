package com.orbesource.cryptora.service;

import com.orbesource.cryptora.model.dto.Average;
import com.orbesource.cryptora.model.dto.Info;
import com.orbesource.cryptora.model.dto.Statistic;
import com.orbesource.cryptora.model.dto.Total;
import com.orbesource.cryptora.model.entity.Quote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@Service
public class StatisticService {

	private static final Logger logger = LoggerFactory.getLogger(StatisticService.class);

	private final QuoteService quoteService;

	public StatisticService(QuoteService quoteService) {
		this.quoteService = quoteService;
	}

	public Statistic calculateStatisticReport(String ticker) {

		List<Quote> quotes = quoteService.getQuotesByTicker(ticker);

		logger.debug("StatisticService | Generate statistic report for the ticker: {}", ticker);

		if (quotes.isEmpty()) {
			var warn = String.format(
					"The ticker with the name %s does not exist or information about it is unavailable.", ticker);
			logger.warn("StatisticService | {}", warn);
			throw new IllegalArgumentException(warn);
		}

		return new Statistic(ticker,
				new Average(calculateAverageOpenPrice(quotes), calculateAverageClosePrice(quotes),
						calculateAverageHighPrice(quotes), calculateAverageLowPrice(quotes),
						calculateAverageTradePrice(quotes), calculateAveragePriceRange(quotes)),
				new Total(calculateTotalVolume(quotes), calculateTotalAmount(quotes)),
				new Info(quotes.size(), LocalDateTime.now(), quotes.stream().map(Quote::getDatetime).findFirst()));
	}

	private BigDecimal calculateAverageOpenPrice(List<Quote> quotes) {
		return calculateAverage(quotes, Quote::getOpenPrice);
	}

	private BigDecimal calculateAverageClosePrice(List<Quote> quotes) {
		return calculateAverage(quotes, Quote::getClosePrice);
	}

	private BigDecimal calculateAverageHighPrice(List<Quote> quotes) {
		return calculateAverage(quotes, Quote::getHighPrice);
	}

	private BigDecimal calculateAverageLowPrice(List<Quote> quotes) {
		return calculateAverage(quotes, Quote::getLowPrice);
	}

	private BigDecimal calculateAveragePriceRange(List<Quote> quotes) {
		return calculateAverage(quotes, quote -> quote.getHighPrice().subtract(quote.getLowPrice()));
	}

	private BigDecimal calculateTotalVolume(List<Quote> quotes) {
		return calculateTotal(quotes, Quote::getVolume);
	}

	private BigDecimal calculateTotalAmount(List<Quote> quotes) {
		return calculateTotal(quotes, Quote::getAmount);
	}

	private BigDecimal calculateAverageTradePrice(List<Quote> quotes) {
		BigDecimal totalAmount = calculateTotalAmount(quotes);
		BigDecimal totalVolume = calculateTotalVolume(quotes);
		return quotes.isEmpty() ? BigDecimal.ZERO : totalAmount.divide(totalVolume, 2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateAverage(List<Quote> quotes, Function<Quote, BigDecimal> function) {
		return quotes.isEmpty() ? BigDecimal.ZERO
				: quotes.stream().map(function).reduce(BigDecimal.ZERO, BigDecimal::add)
						.divide(BigDecimal.valueOf(quotes.size()), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateTotal(List<Quote> quotes, Function<Quote, BigDecimal> function) {
		return quotes.isEmpty() ? BigDecimal.ZERO
				: quotes.stream().map(function).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2,
						RoundingMode.HALF_UP);
	}
}
