package org.acme.model;

import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode
abstract class BaseAccount {

    protected Long accountNumber;
    protected Long customerNumber;
    protected String customerName;
    protected BigDecimal balance;
    protected AccountStatus accountStatus = AccountStatus.OPEN;

    public BaseAccount withdrawFunds(BigDecimal amount) {
        this.setBalance(this.getBalance().subtract(amount));
        return this;
    }
}