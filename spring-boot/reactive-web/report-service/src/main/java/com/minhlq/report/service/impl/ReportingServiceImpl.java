package com.minhlq.report.service.impl;

import com.minhlq.report.entity.Transaction;
import com.minhlq.report.entity.User;
import com.minhlq.report.enums.TransactionStatus;
import com.minhlq.report.kafka.TransactionProducer;
import com.minhlq.report.repository.TransactionRepository;
import com.minhlq.report.repository.UserRepository;
import com.minhlq.report.service.ReportingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

  private final TransactionRepository transactionRepository;

  private final UserRepository userRepository;

  private final TransactionProducer transactionProducer;

  @Override
  public Mono<Transaction> report(Transaction transaction) {
    return updateUserTransactions(transaction)
        .map(
            u -> {
              if (!transaction.getStatus().equals(TransactionStatus.VALID)) {
                transaction.setStatus(
                    u.isAccountLocked()
                        ? TransactionStatus.ACCOUNT_BLOCKED
                        : TransactionStatus.FAILURE);
              }
              return transaction;
            })
        .flatMap(transactionRepository::save);
  }

  @Override
  public void asyncProcess(Transaction transaction) {
    updateUserTransactions(transaction)
        .map(
            u -> {
              if (!transaction.getStatus().equals(TransactionStatus.VALID)) {
                transaction.setStatus(
                    u.isAccountLocked()
                        ? TransactionStatus.ACCOUNT_BLOCKED
                        : TransactionStatus.FAILURE);
                transactionProducer.sendMessage(transaction);
              }
              return transaction;
            })
        .filter(
            t ->
                t.getStatus().equals(TransactionStatus.FAILURE)
                    || t.getStatus().equals(TransactionStatus.ACCOUNT_BLOCKED))
        .flatMap(transactionRepository::save)
        .subscribe();
  }

  @NonNull
  private Mono<User> updateUserTransactions(Transaction transaction) {
    return userRepository
        .findByCardId(transaction.getCardId())
        .map(
            user -> {
              if (TransactionStatus.FRAUDULENT.equals(transaction.getStatus())
                  || TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS.equals(transaction.getStatus())
                  || TransactionStatus.FRAUDULENT_NOTIFY_FAILURE.equals(transaction.getStatus())) {

                // Report the User's account and take automatic action against User's account or
                // card
                user.setFraudulentActivityAttemptCount(
                    user.getFraudulentActivityAttemptCount() + 1);
                user.setAccountLocked(user.getFraudulentActivityAttemptCount() > 3);
                List<Transaction> transactions = new ArrayList<>();
                transactions.add(transaction);
                if (Objects.isNull(user.getFraudulentTransactions())
                    || user.getFraudulentTransactions().isEmpty()) {
                  user.setFraudulentTransactions(transactions);
                } else {
                  user.getFraudulentTransactions().add(transaction);
                }
              }

              log.info("User details: {}", user);
              return user;
            })
        .flatMap(userRepository::save);
  }
}
