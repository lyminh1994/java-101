package com.minhlq.notify.kafka;

import com.minhlq.notify.entity.Transaction;
import com.minhlq.notify.service.NotificationService;
import java.util.function.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionConsumer {

  @Bean
  public Consumer<Transaction> consumeTransaction(NotificationService notificationService) {
    return notificationService::asyncProcess;
  }
}
