package com.minhlq.account.kafka;

import com.minhlq.account.entity.Transaction;
import com.minhlq.account.service.AccountService;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConsumer {

  @Bean
  public Consumer<Transaction> consumeTransaction(AccountService accountService) {
    return accountService::asyncProcess;
  }
}
