package org.acme.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.Account;

@ApplicationScoped //only one instace exists in the application
//The PanacheRepository contains all access methods as for JpaRepository in Spring Data Jpa
//All standard methods are all included
public class AccountRepository implements PanacheRepository<Account> {

    public Account findByAccountNumber(Long accountNumber) {
        return find("accountNumber = ?1", accountNumber).firstResult();
    }
}
