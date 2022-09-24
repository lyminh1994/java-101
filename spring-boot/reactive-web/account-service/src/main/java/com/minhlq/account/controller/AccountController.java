package com.minhlq.account.controller;

import com.minhlq.account.entity.Transaction;
import com.minhlq.account.service.AccountService;
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
@RequestMapping("/account")
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/manage")
  public Mono<Transaction> manage(@RequestBody Transaction transaction) {
    log.info("Process transaction with details in account service: {}", transaction);
    return accountService.manage(transaction);
  }
}
