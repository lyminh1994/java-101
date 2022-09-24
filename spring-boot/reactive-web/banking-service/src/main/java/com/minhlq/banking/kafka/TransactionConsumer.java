package com.minhlq.banking.kafka;

import com.minhlq.banking.entity.Transaction;
import com.minhlq.banking.service.TransactionService;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TransactionConsumer {

  @Bean
  public Consumer<Transaction> consumeTransaction(TransactionService transactionService) {
    return transactionService::asyncProcess;
  }
}
