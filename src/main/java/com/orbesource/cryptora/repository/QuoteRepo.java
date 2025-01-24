package com.orbesource.cryptora.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.orbesource.cryptora.model.entity.Quote;


@Repository
public interface QuoteRepo extends CrudRepository<Quote, String> {
}
