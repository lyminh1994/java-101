package com.minhlq.account.service;

import com.minhlq.account.entity.Transaction;
import reactor.core.publisher.Mono;

public interface AccountService {

  Mono<Transaction> manage(Transaction transaction);

  void asyncProcess(Transaction transaction);
}
