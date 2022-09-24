package com.minhlq.banking.entity;

import com.minhlq.banking.enums.TransactionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@ToString
@NoArgsConstructor
public class Transaction {

  @Id private String transactionId;
  private String date;
  private double amountDeducted;
  private String storeName;
  private String storeId;
  private String cardId;
  private String transactionLocation;
  private TransactionStatus status;
}
