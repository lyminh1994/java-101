package com.minhlq.report.controller;

import com.minhlq.report.entity.Transaction;
import com.minhlq.report.service.ReportingService;
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
@RequestMapping("/report")
public class ReportingController {

  private final ReportingService reportingService;

  @PostMapping
  public Mono<Transaction> report(@RequestBody Transaction transaction) {
    log.info("Process transaction with details in reporting service: {}", transaction);
    return reportingService.report(transaction);
  }
}
