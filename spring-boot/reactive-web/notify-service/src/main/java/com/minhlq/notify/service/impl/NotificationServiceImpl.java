package com.minhlq.notify.service.impl;

import com.minhlq.notify.entity.Transaction;
import com.minhlq.notify.entity.User;
import com.minhlq.notify.enums.TransactionStatus;
import com.minhlq.notify.kafka.TransactionProducer;
import com.minhlq.notify.repository.TransactionRepository;
import com.minhlq.notify.repository.UserRepository;
import com.minhlq.notify.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final TransactionRepository transactionRepository;

  private final UserRepository userRepository;

  private final JavaMailSender emailSender;

  private final TransactionProducer transactionProducer;

  @Override
  public Mono<Transaction> notify(Transaction transaction) {
    return userRepository
        .findByCardId(transaction.getCardId())
        .map(
            u -> {
              if (TransactionStatus.FRAUDULENT.equals(transaction.getStatus())) {

                // Notify user by sending email
                sendMailMessage(transaction, u);
              } else {
                transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE);
              }
              return transaction;
            })
        .onErrorReturn(transaction)
        .flatMap(transactionRepository::save);
  }

  @Override
  public void asyncProcess(Transaction transaction) {
    userRepository
        .findByCardId(transaction.getCardId())
        .map(
            user -> {
              if (transaction.getStatus().equals(TransactionStatus.FRAUDULENT)) {
                try {
                  // Notify user by sending email
                  sendMailMessage(transaction, user);
                } catch (MailException e) {
                  transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_FAILURE);
                }
              }

              return transaction;
            })
        .onErrorReturn(transaction)
        .filter(
            t ->
                TransactionStatus.FRAUDULENT.equals(t.getStatus())
                    || TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS.equals(t.getStatus())
                    || TransactionStatus.FRAUDULENT_NOTIFY_FAILURE.equals(t.getStatus()))
        .map(
            t -> {
              transactionProducer.sendMessage(t);
              return t;
            })
        .flatMap(transactionRepository::save)
        .subscribe();
  }

  private void sendMailMessage(Transaction transaction, User user) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("noreply@baeldung.com");
    message.setTo(user.getEmail());
    message.setSubject("Fraudulent transaction attempt from your card");
    message.setText(
        "An attempt has been made to pay "
            + transaction.getStoreName()
            + " from card "
            + transaction.getCardId()
            + " in the country "
            + transaction.getTransactionLocation()
            + "."
            + " Please report to your bank or block your card.");
    emailSender.send(message);
    transaction.setStatus(TransactionStatus.FRAUDULENT_NOTIFY_SUCCESS);
  }
}
