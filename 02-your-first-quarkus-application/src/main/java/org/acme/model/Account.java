package org.acme.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public class Account {
    private Long accountNumber;
    private Long customerNumber;
    private String customerName;
    private BigDecimal balance;
    private AccountStatus accountStatus = AccountStatus.OPEN;

    public Account() {
    }

    public Account(Long accountNumber, Long customerNumber, String
            customerName, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerNumber = customerNumber;
        this.customerName = customerName;
        this.balance = balance;
    }
    public void markOverdrawn() {
        accountStatus = AccountStatus.OVERDRAWN;
    }
    public void removeOverdrawnStatus() {
        accountStatus = AccountStatus.OPEN;
    }
    public void close() {
        accountStatus = AccountStatus.CLOSED;
        balance = BigDecimal.valueOf(0);
    }
    public void withdrawFunds(BigDecimal amount) {
        balance = balance.subtract(amount);
    }
    public void addFunds(BigDecimal amount) {
        balance = balance.add(amount);
    }

}
