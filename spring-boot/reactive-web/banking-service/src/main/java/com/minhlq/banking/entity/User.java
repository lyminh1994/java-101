package com.minhlq.banking.entity;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@ToString
@NoArgsConstructor
public class User {

  @Id private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String address;
  private String country;
  private String mobile;
  private String gender;
  private String cardId;
  private String accountNumber;
  private String accountType;
  private boolean accountLocked;
  private Long fraudulentActivityAttemptCount;
  private List<Transaction> validTransactions;
  private List<Transaction> fraudulentTransactions;
}
