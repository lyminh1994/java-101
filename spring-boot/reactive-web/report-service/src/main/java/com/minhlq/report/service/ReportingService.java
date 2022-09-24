package com.minhlq.report.service;

import com.minhlq.report.entity.Transaction;
import reactor.core.publisher.Mono;

public interface ReportingService {

  Mono<Transaction> report(Transaction transaction);

  void asyncProcess(Transaction transaction);
}
