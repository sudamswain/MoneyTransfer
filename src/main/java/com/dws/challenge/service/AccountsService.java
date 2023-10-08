package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferDto;
import com.dws.challenge.exception.CustomAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.sun.jdi.InternalException;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class AccountsService {

    // Create a lambda expression for the NotificationService implementation
    NotificationService notificationService = (account, transferDescription) -> {
        String accountId = account.getAccountId();
        String message = transferDescription;
        //send notification by sms or/and email.
        log.info("Sending notification to owner of {}: {}", account.getAccountId(), transferDescription);
    };

    @Getter
    private final AccountsRepository accountsRepository;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }


    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }



    public boolean transferMoney(TransferDto transferDto) throws Exception {
        Object lock1 = new Object();
        Object lock2 = new Object();
        if (transferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("amount should not negative"); // Invalid amount
        }

        Account from = this.accountsRepository.getAccount(transferDto.getFromAccountId());
        Account to = this.accountsRepository.getAccount(transferDto.getToAccountId());
        if (from == null) {
            throw new CustomAccountIdException("from account is not available");
        }
        if (to == null) {
            throw new CustomAccountIdException("to account is not available");
        }
        if (from.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new CustomAccountIdException("balance less than amount");
        }

        synchronized (from) {
            synchronized (to) {
                from.withdraw(transferDto.getAmount());
                to.deposit(transferDto.getAmount());
                String fromMessage = "Transferred $" + transferDto.getAmount() + " to account " + to.getAccountId();
                String toMessage = "Received $" + transferDto.getAmount() + " from account " + from.getAccountId();
                notificationService.notifyAboutTransfer(from, fromMessage);
                notificationService.notifyAboutTransfer(to, toMessage);
                return true; // Transfer successful
            }
        }
    }
}
