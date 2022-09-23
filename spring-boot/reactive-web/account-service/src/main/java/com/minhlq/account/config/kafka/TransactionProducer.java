package com.minhlq.account.config.kafka;

import com.minhlq.account.entity.Transaction;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProducer {

  private final StreamBridge streamBridge;

  public void sendMessage(Transaction transaction) {
    Message<Transaction> message =
        MessageBuilder.withPayload(transaction)
            .setHeader(
                KafkaHeaders.MESSAGE_KEY,
                transaction.getTransactionId().getBytes(StandardCharsets.UTF_8))
            .build();
    log.info(
        "Transaction processed to dispatch: {}; Message dispatch successful: {}",
        message,
        streamBridge.send("transaction-out-0", message));
  }
}
