package com.dzenthai.cryptora.analyze.repository;

import com.dzenthai.cryptora.analyze.entity.Quote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface QuoteRepo extends MongoRepository<Quote, Long> {

    void deleteAllByTimeLessThan(LocalDateTime time);

}
