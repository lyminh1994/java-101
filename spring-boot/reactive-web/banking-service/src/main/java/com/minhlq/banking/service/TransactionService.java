package com.minhlq.banking.service;

import com.minhlq.banking.entity.Transaction;
import reactor.core.publisher.Mono;

public interface TransactionService {

  Mono<Transaction> process(Transaction transaction);

  void asyncProcess(Transaction transaction);
}
