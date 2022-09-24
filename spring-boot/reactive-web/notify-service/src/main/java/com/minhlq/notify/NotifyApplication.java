package com.minhlq.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class NotifyApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotifyApplication.class, args);
  }
}
