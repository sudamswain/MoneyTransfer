package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.logging.Logger;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferDto;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;




  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }


  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  @Test
  public void testTransferMoneySuccessful() throws Exception {
    // Create test data
    Account fromAccount = new Account("1", new BigDecimal("1000.0"));
    Account toAccount = new Account("2", new BigDecimal("2000.0"));
    AccountsRepository accountsRepository =  mock(AccountsRepository.class);
    AccountsService accountsService = new AccountsService(accountsRepository);
    TransferDto transferDto = new TransferDto("1", "2", new BigDecimal("500.0"));

    // Mock account retrieval from repository
    when(accountsRepository.getAccount("1")).thenReturn(fromAccount);
    when(accountsRepository.getAccount("2")).thenReturn(toAccount);

    // Call the transferMoney method
    boolean result = accountsService.transferMoney(transferDto);

    // Verify that the transfer was successful
    assertTrue(result);

    // Verify that the balances were updated correctly
    assertEquals(new BigDecimal("500.0"), fromAccount.getBalance());
    assertEquals(new BigDecimal("2500.0"), toAccount.getBalance());

    // Verify that notifications were sent
    //  verify(notificationService, times(2)).notifyAboutTransfer(any(Account.class), anyString());
  }

  @Test
  public void testTransferMoneyInvalidAmount() {
    // Create test data with an invalid amount
    TransferDto transferDto = new TransferDto("1", "2", BigDecimal.ZERO);

    // Call the transferMoney method and expect an exception
    assertThrows(Exception.class, () -> accountsService.transferMoney(transferDto));
  }


  @Test
  public void testNotificationService() {
    // Mock the logger
    Logger loggerMock = Mockito.mock(Logger.class);

    // Create an Account for testing
    Account account = new Account("1", new BigDecimal("1000.0"));

    // Create a lambda expression for the NotificationService implementation
    NotificationService notificationService = (accountLambda, transferDescription) -> {
      String accountId = accountLambda.getAccountId();
      String message = transferDescription;
      // Log the message using the mocked logger
      loggerMock.info("Sending notification to owner of "+ accountId+":"+ transferDescription);
    };

    // Call the lambda expression
    notificationService.notifyAboutTransfer(account, "Transfer Description");

    // Verify that the logger's info method was called with the expected parameters
    Mockito.verify(loggerMock).info("Sending notification to owner of "+ account.getAccountId()+":Transfer Description");

  }
}