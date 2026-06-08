package dev.fascodes.budgetApp.transaction.repository;

import dev.fascodes.budgetApp.transaction.model.Transaction;
import dev.fascodes.budgetApp.transaction.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.account.id = :accountId")
    boolean existsByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type")
    BigDecimal sumAmountByAccountAndType(@Param("accountId") Long accountId, @Param("type") TransactionType type);

    @Query("SELECT t.category.name as categoryName, SUM(t.amount) as total FROM Transaction t WHERE t.account.id = :accountId AND t.type = :type GROUP BY t.category.name")
    List<CategoryExpenseProjection> sumByCategory(@Param("accountId") Long accountId, @Param("type") TransactionType type);
}
