package dev.fascodes.budgetApp.transaction.service;

import dev.fascodes.budgetApp.account.model.Account;
import dev.fascodes.budgetApp.account.repository.AccountRepository;
import dev.fascodes.budgetApp.common.CommonTestConfiguration;
import dev.fascodes.budgetApp.transaction.dto.TransactionRequest;
import dev.fascodes.budgetApp.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration tests for the pessimistic lock guarding account balance updates
 * ({@link AccountRepository#findByIdWithLock}, used by {@link TransactionService#addTransaction}).
 *
 * <p>Runs against a real PostgreSQL via Testcontainers - {@code SELECT ... FOR UPDATE} semantics
 * cannot be reproduced on an in-memory database. The Hikari pool is enlarged so that the
 * concurrency test is bounded by the row lock, not by connection starvation.
 */
@SpringBootTest
@Import(CommonTestConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.hikari.maximum-pool-size=40",
        "spring.jpa.show-sql=false"
})
class TransactionServiceRaceConditionTest {

    private static final BigDecimal INITIAL = new BigDecimal("100.00");

    @Autowired private TransactionService transactionService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionTemplate txTemplate;
    @Autowired private JdbcTemplate jdbc;

    private Long accountId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM transactions");
        jdbc.update("DELETE FROM accounts");
        jdbc.update("DELETE FROM categories");

        categoryId = jdbc.queryForObject(
                "INSERT INTO categories (name) VALUES ('Race condition') RETURNING id", Long.class);
        accountId = jdbc.queryForObject(
                "INSERT INTO accounts (name, balance) VALUES ('Race condition', ?) RETURNING id",
                Long.class, INITIAL);
    }

    @Test
    void secondWithdrawalWaitsForLockAndSeesCommittedBalance() throws Exception {
        var t1HasLock = new CountDownLatch(1);
        var t1MayFinish = new CountDownLatch(1);
        var balanceSeenByT2 = new AtomicReference<BigDecimal>();
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            // T1 takes the row lock and holds it until we let it commit.
            Future<?> t1 = pool.submit(() -> txTemplate.executeWithoutResult(status -> {
                accountRepository.findByIdWithLock(accountId).orElseThrow();   // SELECT ... FOR UPDATE
                t1HasLock.countDown();
                awaitUninterruptibly(t1MayFinish);
                transactionService.addTransaction(expense(new BigDecimal("30.00")));
            }));

            t1HasLock.await();

            // T2 wants the same row - it must block instead of reading the stale balance.
            Future<?> t2 = pool.submit(() -> txTemplate.executeWithoutResult(status -> {
                Account account = accountRepository.findByIdWithLock(accountId).orElseThrow();
                balanceSeenByT2.set(account.getBalance());
                transactionService.addTransaction(expense(new BigDecimal("50.00")));
            }));

            // Deterministic proof that T2 is parked on the lock, instead of assuming it got there.
            await().atMost(5, SECONDS).until(() -> blockedOnLockCount() == 1);
            assertThat(t2.isDone())
                    .as("T2 must not finish while T1 holds the lock")
                    .isFalse();

            t1MayFinish.countDown();
            t1.get(5, SECONDS);
            t2.get(5, SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertThat(balanceSeenByT2.get())
                .as("after the lock is released T2 must see T1's committed balance")
                .isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(balanceOf(accountId)).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @RepeatedTest(20)
    void concurrentDebitsPreserveBalance() throws Exception {
        int threads = 32;
        var ready = new CountDownLatch(threads);
        var go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        var futures = new ArrayList<Future<?>>();

        try {
            for (int i = 0; i < threads; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    transactionService.addTransaction(expense(BigDecimal.ONE));
                    return null;
                }));
            }
            ready.await();                              // every thread parked on the start gate
            go.countDown();                             // release them at once
            for (Future<?> f : futures) {
                f.get(30, SECONDS);                     // propagates failures from the worker threads
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(balanceOf(accountId))
                .isEqualByComparingTo(INITIAL.subtract(BigDecimal.valueOf(threads)));
        assertThat(transactionCount()).isEqualTo(threads);
    }

    // --- helpers ---

    private TransactionRequest expense(BigDecimal amount) {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(accountId);
        request.setCategoryId(categoryId);
        request.setType(TransactionType.EXPENSE);
        request.setAmount(amount);
        request.setDate(LocalDateTime.now());
        return request;
    }

    private BigDecimal balanceOf(Long id) {
        return jdbc.queryForObject("SELECT balance FROM accounts WHERE id = ?", BigDecimal.class, id);
    }

    private int transactionCount() {
        return jdbc.queryForObject("SELECT count(*) FROM transactions WHERE account_id = ?", Integer.class, accountId);
    }

    /** Backends of this database currently waiting on a heavyweight (row/table) lock. */
    private int blockedOnLockCount() {
        List<Integer> blocked = jdbc.queryForList("""
                SELECT pid FROM pg_stat_activity
                WHERE wait_event_type = 'Lock'
                  AND datname = current_database()
                  AND pid <> pg_backend_pid()
                """, Integer.class);
        return blocked.size();
    }

    private static void awaitUninterruptibly(CountDownLatch latch) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    latch.await();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
