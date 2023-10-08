package com.dws.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;



@Data
public class Account implements Serializable {

  @NotNull
  @NotEmpty
  private final String accountId;

  @JsonIgnore
  private final Object lock = new Object();

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }

  public void deposit(BigDecimal amount) {
    synchronized (lock) {
      if (amount.compareTo(BigDecimal.ZERO) > 0) {
        balance = balance.add(amount);
      }
    }
  }

  public boolean withdraw(BigDecimal amount) {
    synchronized (lock) {
      if (amount.compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(amount) >= 0) {
        balance = balance.subtract(amount);
        return true; // Withdrawal successful
      }
      return false; // Insufficient balance
    }
  }



}
