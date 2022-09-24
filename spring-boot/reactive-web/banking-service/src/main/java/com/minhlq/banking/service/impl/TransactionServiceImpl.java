package com.minhlq.banking.service.impl;

import com.minhlq.banking.kafka.TransactionProducer;
import com.minhlq.banking.entity.Transaction;
import com.minhlq.banking.enums.TransactionStatus;
import com.minhlq.banking.repository.TransactionRepository;
import com.minhlq.banking.repository.UserRepository;
import com.minhlq.banking.service.TransactionService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private static final String NOTIFY_SERVICE_URL =
      "http://localhost:8081/notify/fraudulent-transaction";
  private static final String REPORT_SERVICE_URL = "http://localhost:8082/report";
  private static final String ACCOUNT_SERVICE_URL = "http://localhost:8083/account/manage";

  private final TransactionRepository transactionRepository;

  private final UserRepository userRepository;

  private final WebClient webClient;

  private final TransactionProducer transactionProducer;

  @Override
  public Mono<Transaction> process(Transaction transaction) {
    return Mono.just(transaction)
        .flatMap(transactionRepository::save)
        .flatMap(
            t ->
                userRepository
                    .findByCardId(t.getCardId())
                    .mapNotNull(
                        user -> {
                          log.info("User details: {}", user);
                          if (t.getStatus().equals(TransactionStatus.INITIATED)) {
                            // Check whether the card details are valid or not
                            if (Objects.isNull(user)) {
                              t.setStatus(TransactionStatus.CARD_INVALID);
                            }

                            // Check whether the account is blocked or not
                            else if (user.isAccountLocked()) {
                              t.setStatus(TransactionStatus.ACCOUNT_BLOCKED);
                            } else {
                              // Check if it's a valid transaction or not. The Transaction would be
                              // considered valid
                              // if it has been requested from the same home country of the user,
                              // else will be considered
                              // as fraudulent
                              if (user.getCountry().equalsIgnoreCase(t.getTransactionLocation())) {
                                t.setStatus(TransactionStatus.VALID);

                                // Call Reporting Service to report valid transaction to bank and
                                // deduct amount if funds available
                                return syncTransactionToService(
                                    t, REPORT_SERVICE_URL, ACCOUNT_SERVICE_URL);
                              }

                              t.setStatus(TransactionStatus.FRAUDULENT);

                              // Call User Notification service to notify for a fraudulent
                              // transaction
                              // attempt from the User's card
                              return syncTransactionToService(
                                  t, NOTIFY_SERVICE_URL, REPORT_SERVICE_URL);
                            }
                          } else {
                            // For any other case, the transaction will be considered failure
                            t.setStatus(TransactionStatus.FAILURE);
                          }
                          return t;
                        }));
  }

  @Nullable
  private Transaction syncTransactionToService(
      Transaction transaction, String reportServiceUrl, String accountServiceUrl) {
    return webClient
        .post()
        .uri(reportServiceUrl)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(transaction))
        .retrieve()
        .bodyToMono(Transaction.class)
        .zipWhen(
            t1 ->
                // Call Account Manager service to process the transaction and send the money
                webClient
                    .post()
                    .uri(accountServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(transaction))
                    .retrieve()
                    .bodyToMono(Transaction.class)
                    .log(),
            (t1, t2) -> t2)
        .log()
        .share()
        .block();
  }

  @Override
  public void asyncProcess(Transaction transaction) {
    userRepository
        .findByCardId(transaction.getCardId())
        .map(
            u -> {
              if (transaction.getStatus().equals(TransactionStatus.INITIATED)) {
                log.info("Consumed message for processing: {}", transaction);
                log.info("User details: {}", u);
                // Check whether the card details are valid or not
                if (Objects.isNull(u)) {
                  transaction.setStatus(TransactionStatus.CARD_INVALID);
                }

                // Check whether the account is blocked or not
                else if (u.isAccountLocked()) {
                  transaction.setStatus(TransactionStatus.ACCOUNT_BLOCKED);
                } else {
                  // Check if it's a valid transaction or not. The Transaction would be considered
                  // valid
                  // if it has been requested from the same home country of the user, else will be
                  // considered
                  // as fraudulent
                  if (u.getCountry().equalsIgnoreCase(transaction.getTransactionLocation())) {
                    transaction.setStatus(TransactionStatus.VALID);
                  } else {
                    transaction.setStatus(TransactionStatus.FRAUDULENT);
                  }
                }
                transactionProducer.sendMessage(transaction);
              }
              return transaction;
            })
        .filter(
            t ->
                t.getStatus().equals(TransactionStatus.VALID)
                    || t.getStatus().equals(TransactionStatus.FRAUDULENT)
                    || t.getStatus().equals(TransactionStatus.CARD_INVALID)
                    || t.getStatus().equals(TransactionStatus.ACCOUNT_BLOCKED))
        .flatMap(transactionRepository::save)
        .subscribe();
  }
}
