package com.minhlq.report.kafka;

import com.minhlq.report.entity.Transaction;
import com.minhlq.report.service.ReportingService;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConsumer {

  @Bean
  public Consumer<Transaction> consumeTransaction(ReportingService reportingService) {
    return reportingService::asyncProcess;
  }
}
