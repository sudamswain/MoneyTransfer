package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.dto.TransferDto;
import com.dws.challenge.exception.CustomAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
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
        if (transferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Amount should not be negative"); // Invalid amount
        }

        Account from = this.accountsRepository.getAccount(transferDto.getFromAccountId());
        Account to = this.accountsRepository.getAccount(transferDto.getToAccountId());

        if (from == null) {
            throw new CustomAccountIdException("From account is not available");
        }
        if (to == null) {
            throw new CustomAccountIdException("To account is not available");
        }

        BigDecimal transferAmount = transferDto.getAmount();

        // Sort accounts to acquire locks in a consistent order
        Account firstAccount;
        Account secondAccount;

        if (from.getAccountId().compareTo(to.getAccountId()) < 0) {
            firstAccount = from;
            secondAccount = to;
        } else {
            firstAccount = to;
            secondAccount = from;
        }

        synchronized (firstAccount.getLock()) {
            synchronized (secondAccount.getLock()) {
                if (from.getBalance().compareTo(transferAmount) < 0) {
                    throw new CustomAccountIdException("Balance is less than the transfer amount");
                }

                from.withdraw(transferAmount);
                to.deposit(transferAmount);

                String fromMessage = "Transferred " + transferAmount + " to account " + to.getAccountId();
                String toMessage = "Received " + transferAmount + " from account " + from.getAccountId();

                notificationService.notifyAboutTransfer(from, fromMessage);
                notificationService.notifyAboutTransfer(to, toMessage);

                return true; // Transfer successful
            }
        }
    }

}
