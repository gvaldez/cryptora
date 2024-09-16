package com.dzenthai.cryptora.analyze.service;

import com.dzenthai.cryptora.analyze.entity.Quote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void saveQuotesToRedis(List<Quote> quotes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        for (Quote quote : quotes) {
            String key = "quote:" + quote.getTicker() + ":" + quote.getTime().format(formatter);
            String value = quote.getPrice().toString();
            log.debug("Redis Service | Saving quotes to Redis database, key: {}, value: {}", key, value);
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public List<Quote> getQuotesFromRedis() {
        log.debug("Redis Service | Fetching list of quotes from Redis database");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        return Objects.requireNonNull(redisTemplate.keys("quote:*")).stream()
                .map(key -> {
                    try {
                        String[] parts = key.split(":");
                        if (parts.length < 3) {
                            log.warn("Redis Service | Incorrect key format in Redis: {}", key);
                            return null;
                        }
                        String ticker = parts[1];
                        String timeString = String.join(":", parts[2], parts[3], parts[4]);
                        LocalDateTime time = LocalDateTime.parse(timeString, formatter);
                        BigDecimal price = new BigDecimal(Objects.requireNonNull(redisTemplate.opsForValue().get(key)));
                        return Quote.builder().ticker(ticker).time(time).price(price).build();
                    } catch (DateTimeParseException e) {
                        log.error("Redis Service | Error parsing time from key: {}", key, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public void deleteQuotesFromRedis() {
        List<Quote> quotes = getQuotesFromRedis();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusMinutes(33).plusSeconds(20);

        List<String> keysToDelete = quotes.stream()
                .filter(quote -> quote.getTime().isBefore(oneWeekAgo))
                .map(quote -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    return "quote:" + quote.getTicker() + ":" + quote.getTime().format(formatter);
                })
                .collect(Collectors.toList());

        if (!keysToDelete.isEmpty()) {
            log.debug("Redis Service | Deleting quotes from Redis database, keys: {}", keysToDelete);
            redisTemplate.delete(keysToDelete);
        } else {
            log.debug("Redis Service | No quotes to delete; All quotes are up to date.");
        }
    }
}
