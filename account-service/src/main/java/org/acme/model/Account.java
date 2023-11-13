package org.acme.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@NamedQuery(name = "Accounts.findAll",
        query = "SELECT a FROM Account a ORDER BY a.accountNumber")
@NamedQuery(name = "Accounts.findByAccountNumber", query = "SELECT a FROM Account a WHERE a.accountNumber = :accountNumber ORDER BY a.accountNumber")
@Getter
@Setter
public class Account extends BaseAccount {
    public Account() {
    }

    @Builder
    public Account(Long id, Long accountNumber, Long customerNumber, String customerName, BigDecimal balance) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountStatus = AccountStatus.OPEN;
        this.customerNumber = customerNumber;
        this.customerName = customerName;
        this.balance = balance;
    }

    @Id
    @SequenceGenerator(name = "accountsSequence", sequenceName =
            "accounts_id_seq",
            allocationSize = 1, initialValue = 10)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "accountsSequence")
    private Long id;
}
