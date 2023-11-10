package org.acme.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@NamedQuery(name = "Accounts.findAll",
        query = "SELECT a FROM Account a ORDER BY a.accountNumber")
@NamedQuery(name = "Accounts.findByAccountNumber", query = "SELECT a FROM Account a WHERE a.accountNumber = :accountNumber ORDER BY a.accountNumber")
@Getter
@Setter
@EqualsAndHashCode
public class Account {
    @Id
    @SequenceGenerator(name = "accountsSequence", sequenceName =
            "accounts_id_seq",
            allocationSize = 1, initialValue = 10)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "accountsSequence")
    private Long id;
    private Long accountNumber;

    private Long customerNumber;
    private String customerName;
    private BigDecimal balance;
    private AccountStatus accountStatus = AccountStatus.OPEN;

    public Account withdrawFunds(BigDecimal amout) {
        this.setBalance(this.getBalance().subtract(amout));
        return this;
    }
}