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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppInit implements ApplicationListener<ApplicationReadyEvent> {

  public static final String USERS_JSON = "/json/users.json";
  public static final String TRANSACTIONS_JSON = "/json/transactions.json";

  private final ObjectMapper mapper;

  private final UserRepository userRepository;

  private final TransactionProducer transactionProducer;

  @Override
  public void onApplicationEvent(@NonNull ApplicationReadyEvent applicationReadyEvent) {
    log.info("Application started");
    TypeReference<List<User>> typeReferenceUser = new TypeReference<>() {};
    InputStream inputStreamUser = TypeReference.class.getResourceAsStream(USERS_JSON);
    try {
      List<User> usersList = mapper.readValue(inputStreamUser, typeReferenceUser);
      usersList.forEach(
          u -> {
            User user = userRepository.findByCardId(u.getCardId()).share().block();
            if (Objects.isNull(user)) {
              userRepository.save(u).subscribe();
            }
          });
      log.info("User Saved!");
    } catch (IOException e) {
      log.error("Unable to save User: " + e.getMessage());
    }

    TypeReference<List<Transaction>> typeReferenceTransaction = new TypeReference<>() {};
    InputStream inputStreamTransaction = TypeReference.class.getResourceAsStream(TRANSACTIONS_JSON);
    try {
      List<Transaction> transactionsList =
          mapper.readValue(inputStreamTransaction, typeReferenceTransaction);
      transactionsList.forEach(transactionProducer::sendMessage);
      log.info("Transactions Dispatched to Kafka topic!");
    } catch (IOException e) {
      log.error("Unable to dispatch transactions to Kafka Topic: " + e.getMessage());
    }
  }
}
