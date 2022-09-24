package com.minhlq.notify.service;

import com.minhlq.notify.entity.Transaction;
import reactor.core.publisher.Mono;

public interface NotificationService {

  Mono<Transaction> notify(Transaction transaction);

  void asyncProcess(Transaction transaction);
}
