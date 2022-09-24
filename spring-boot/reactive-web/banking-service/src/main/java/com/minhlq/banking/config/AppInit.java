package com.minhlq.banking.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minhlq.banking.entity.Transaction;
import com.minhlq.banking.entity.User;
import com.minhlq.banking.kafka.TransactionProducer;
import com.minhlq.banking.repository.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppInit implements ApplicationListener<ApplicationReadyEvent> {

  private final UserRepository userRepo;

  private final TransactionProducer producer;

  @Override
  public void onApplicationEvent(@NotNull ApplicationReadyEvent applicationReadyEvent) {
    log.info("Application started");
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<User>> typeReferenceUser = new TypeReference<>() {};
    InputStream inputStreamUser = TypeReference.class.getResourceAsStream("/json/users.json");
    try {
      List<User> usersList = mapper.readValue(inputStreamUser, typeReferenceUser);
      usersList.forEach(
          u -> {
            User user = userRepo.findByCardId(u.getCardId()).share().block();
            if (Objects.isNull(user)) {
              userRepo.save(u).subscribe();
            }
          });
      log.info("User Saved!");
    } catch (IOException e) {
      log.error("Unable to save User: " + e.getMessage());
    }

    TypeReference<List<Transaction>> typeReferenceTransaction = new TypeReference<>() {};
    InputStream inputStreamTransaction =
        TypeReference.class.getResourceAsStream("/json/transactions.json");
    try {
      List<Transaction> transactionsList =
          mapper.readValue(inputStreamTransaction, typeReferenceTransaction);
      transactionsList.forEach(producer::sendMessage);
      log.info("Transactions Dispatched to Kafka topic!");
    } catch (IOException e) {
      log.error("Unable to dispatch transactions to Kafka Topic: " + e.getMessage());
    }
  }
}
