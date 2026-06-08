package dev.fascodes.budgetApp.transaction.repository;

import dev.fascodes.budgetApp.transaction.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TransactionSpecification {

    public static Specification<Transaction> hasAccount(Long accountId) {
        return (root, query, cb) -> cb.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transaction> dateFrom(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from.atStartOfDay());
    }

    public static Specification<Transaction> dateTo(LocalDate to) {
        return (root, query, cb) -> cb.lessThan(root.get("date"), to.plusDays(1).atStartOfDay());
    }

    public static Specification<Transaction> hasCategory(String categoryName) {
        return (root, query, cb) -> cb.equal(
                cb.lower(root.join("category").get("name")),
                categoryName.toLowerCase()
        );
    }
}
