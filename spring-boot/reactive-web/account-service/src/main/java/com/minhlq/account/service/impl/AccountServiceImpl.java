package com.minhlq.account.service.impl;

import com.minhlq.account.entity.Transaction;
import com.minhlq.account.entity.User;
import com.minhlq.account.enums.TransactionStatus;
import com.minhlq.account.kafka.TransactionProducer;
import com.minhlq.account.repository.TransactionRepository;
import com.minhlq.account.repository.UserRepository;
import com.minhlq.account.service.AccountService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final TransactionRepository transactionRepository;

  private final UserRepository userRepository;

  private final TransactionProducer transactionProducer;

  @Override
  public Mono<Transaction> manage(Transaction transaction) {
    return updateUserTransactions(transaction)
        .map(
            user -> {
              if (TransactionStatus.VALID.equals(transaction.getStatus())) {
                transaction.setStatus(TransactionStatus.SUCCESS);
              }

              return transaction;
            })
        .flatMap(transactionRepository::save);
  }

  @Override
  public void asyncProcess(Transaction transaction) {
    updateUserTransactions(transaction)
        .map(
            user -> {
              if (TransactionStatus.VALID.equals(transaction.getStatus())) {
                transaction.setStatus(TransactionStatus.SUCCESS);
                transactionProducer.sendMessage(transaction);
              }

              return transaction;
            })
        .filter(
            t ->
                TransactionStatus.VALID.equals(t.getStatus())
                    || TransactionStatus.SUCCESS.equals(t.getStatus()))
        .flatMap(transactionRepository::save)
        .subscribe();
  }

  private Mono<User> updateUserTransactions(Transaction transaction) {
    return userRepository
        .findByCardId(transaction.getCardId())
        .map(
            user -> {
              if (TransactionStatus.VALID.equals(transaction.getStatus())) {
                List<Transaction> transactions = new ArrayList<>();
                transactions.add(transaction);
                if (Objects.isNull(user.getValidTransactions())
                    || user.getValidTransactions().isEmpty()) {
                  user.setValidTransactions(transactions);
                } else {
                  user.getValidTransactions().add(transaction);
                }
              }

              log.info("User details: {}", user);
              return user;
            })
        .flatMap(userRepository::save);
  }
}
