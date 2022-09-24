package com.minhlq.notify.controller;

import com.minhlq.notify.entity.Transaction;
import com.minhlq.notify.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notify")
public class NotificationController {

  private final NotificationService notificationService;

  @PostMapping("/fraudulent-transaction")
  public Mono<Transaction> notify(@RequestBody Transaction transaction) {
    log.info("Process transaction with details and notify user: {}", transaction);
    return notificationService.notify(transaction);
  }
}
