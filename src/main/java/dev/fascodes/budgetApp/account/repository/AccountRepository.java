package dev.fascodes.budgetApp.account.repository;

import dev.fascodes.budgetApp.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

}
