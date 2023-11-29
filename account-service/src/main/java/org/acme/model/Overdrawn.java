package org.acme.model;

import java.math.BigDecimal;

public class Overdrawn {
    public Long accountNumber;
    public Long customerNumber;
    public BigDecimal balance;
    public BigDecimal overdraftLimit;

    public Overdrawn(Long accountNumber, Long customerNumber, BigDecimal
            balance, BigDecimal overdraftLimit) {
        this.accountNumber = accountNumber;
        this.customerNumber = customerNumber;
        this.balance = balance;
        this.overdraftLimit = overdraftLimit;
    }

    public Overdrawn(Account entity) {
        this.accountNumber = entity.getAccountNumber();
        this.customerNumber = entity.getCustomerNumber();
        this.balance = entity.getBalance();
        this.overdraftLimit = entity.getOverdraftLimit();
    }

    public Overdrawn(AccountAr entity) {
        this.accountNumber = entity.accountNumber;
        this.customerNumber = entity.customerNumber;
        this.balance = entity.balance;
        this.overdraftLimit = entity.overdraftLimit;
    }
}