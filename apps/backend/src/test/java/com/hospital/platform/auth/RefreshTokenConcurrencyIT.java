package com.hospital.platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.platform.auth.dto.LoginResponseDTO;
import com.hospital.platform.auth.dto.RefreshTokenRequestDTO;
import com.hospital.platform.auth.exception.InvalidRefreshTokenException;
import com.hospital.platform.auth.service.AuthService;
import com.hospital.platform.auth.service.CreatedRefreshToken;
import com.hospital.platform.auth.service.RefreshTokenService;
import com.hospital.platform.users.service.AuthenticatedUser;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class RefreshTokenConcurrencyIT {

    private static final UUID USER_ID = UUID.fromString("88888888-8888-8888-8888-888888888881");
    private static final String JWT_SECRET = "refresh-token-concurrency-integration-secret";

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_refresh_concurrency_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("hospital.security.jwt-secret", () -> JWT_SECRET);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private ExecutorService executor;
    private CountDownLatch activeTransactionRelease;

    @BeforeEach
    void setUpUser() {
        dropSuccessorFailureTrigger();
        jdbcTemplate.update("delete from refresh_tokens where user_id = ?", USER_ID);
        jdbcTemplate.update("delete from users where id = ?", USER_ID);
        jdbcTemplate.update(
                "insert into users (id, username, email, password_hash, enabled) values (?, ?, ?, ?, true)",
                USER_ID,
                "refresh-concurrency-user",
                "refresh-concurrency-user@example.test",
                "test-password-hash"
        );
        executor = Executors.newFixedThreadPool(2);
        activeTransactionRelease = new CountDownLatch(0);
    }

    @AfterEach
    void tearDown() {
        activeTransactionRelease.countDown();
        executor.shutdownNow();
        awaitExecutorTermination();
        dropSuccessorFailureTrigger();
    }

    @Test
    void sameRefreshTokenCanBeConsumedSuccessfullyOnlyOnceConcurrently() throws Exception {
        CreatedRefreshToken original = refreshTokenService.issueToken(authenticatedUser());
        CountDownLatch firstRefreshCompleted = new CountDownLatch(1);
        CountDownLatch allowFirstCommit = new CountDownLatch(1);
        activeTransactionRelease = allowFirstCommit;
        CountDownLatch secondTransactionStarted = new CountDownLatch(1);
        AtomicInteger firstBackendPid = new AtomicInteger();
        AtomicInteger secondBackendPid = new AtomicInteger();

        Future<RefreshAttempt> first = executor.submit(() -> refreshHoldingTransaction(
                original.token(), firstBackendPid, firstRefreshCompleted, allowFirstCommit));

        assertThat(firstRefreshCompleted.await(10, TimeUnit.SECONDS)).isTrue();

        Future<RefreshAttempt> second = executor.submit(() -> refreshInIndependentTransaction(
                original.token(), secondBackendPid, secondTransactionStarted));

        assertThat(secondTransactionStarted.await(10, TimeUnit.SECONDS)).isTrue();
        try {
            assertThat(awaitPostgreSqlLockContention(firstBackendPid.get(), secondBackendPid.get())).isTrue();
        } finally {
            allowFirstCommit.countDown();
        }

        RefreshAttempt firstResult = first.get(20, TimeUnit.SECONDS);
        RefreshAttempt secondResult = second.get(20, TimeUnit.SECONDS);

        assertThat(firstResult.successful()).isTrue();
        assertThat(secondResult.successful()).isFalse();
        assertThat(secondResult.failureType()).isEqualTo(InvalidRefreshTokenException.class);

        Integer totalTokens = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where user_id = ?",
                Integer.class,
                USER_ID
        );
        Integer activeTokens = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens "
                        + "where user_id = ? and revoked_at is null and expires_at > current_timestamp",
                Integer.class,
                USER_ID
        );
        Integer consumedOriginal = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where token_hash = ? and revoked_at is not null",
                Integer.class,
                original.tokenHash()
        );

        assertThat(totalTokens).isEqualTo(2);
        assertThat(activeTokens).isEqualTo(1);
        assertThat(consumedOriginal).isEqualTo(1);
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDTO(original.token())))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void successorInsertFailureRollsBackOriginalConsumption() {
        CreatedRefreshToken original = refreshTokenService.issueToken(authenticatedUser());
        createSuccessorFailureTrigger(original.tokenHash());

        try {
            assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequestDTO(original.token())))
                    .isInstanceOf(RuntimeException.class)
                    .hasStackTraceContaining("forced successor insert failure after original consumption");
        } finally {
            dropSuccessorFailureTrigger();
        }

        Boolean originalStillActive = jdbcTemplate.queryForObject(
                "select revoked_at is null from refresh_tokens where token_hash = ?",
                Boolean.class,
                original.tokenHash()
        );
        Integer successors = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where user_id = ? and token_hash <> ?",
                Integer.class,
                USER_ID,
                original.tokenHash()
        );
        Integer totalTokens = jdbcTemplate.queryForObject(
                "select count(*) from refresh_tokens where user_id = ?",
                Integer.class,
                USER_ID
        );

        assertThat(originalStillActive).isTrue();
        assertThat(successors).isZero();
        assertThat(totalTokens).isEqualTo(1);
    }

    private RefreshAttempt refreshHoldingTransaction(
            String token,
            AtomicInteger backendPid,
            CountDownLatch refreshCompleted,
            CountDownLatch allowCommit
    ) {
        try {
            return new TransactionTemplate(transactionManager).execute(status -> {
                backendPid.set(currentBackendPid());
                LoginResponseDTO response = authService.refresh(new RefreshTokenRequestDTO(token));
                refreshCompleted.countDown();
                await(allowCommit, "Timed out waiting to commit the token-consuming transaction");
                return new RefreshAttempt(true, response.refreshToken(), null);
            });
        } catch (RuntimeException exception) {
            return new RefreshAttempt(false, null, exception.getClass());
        }
    }

    private RefreshAttempt refreshInIndependentTransaction(
            String token,
            AtomicInteger backendPid,
            CountDownLatch transactionStarted
    ) {
        try {
            return new TransactionTemplate(transactionManager).execute(status -> {
                backendPid.set(currentBackendPid());
                transactionStarted.countDown();
                LoginResponseDTO response = authService.refresh(new RefreshTokenRequestDTO(token));
                return new RefreshAttempt(true, response.refreshToken(), null);
            });
        } catch (RuntimeException exception) {
            return new RefreshAttempt(false, null, exception.getClass());
        }
    }

    private int currentBackendPid() {
        Integer backendPid = jdbcTemplate.queryForObject("select pg_backend_pid()", Integer.class);
        if (backendPid == null) {
            throw new IllegalStateException("PostgreSQL did not return a backend PID");
        }
        return backendPid;
    }

    private boolean awaitPostgreSqlLockContention(int blockerPid, int blockedPid) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            Boolean blocked = jdbcTemplate.queryForObject("""
                    select exists (
                        select 1
                        from pg_stat_activity activity
                        where activity.pid = ?
                          and activity.datname = current_database()
                          and activity.wait_event_type = 'Lock'
                          and ? = any(pg_blocking_pids(activity.pid))
                          and lower(activity.query) like '%refresh_tokens%'
                    )
                    """, Boolean.class, blockedPid, blockerPid);
            if (Boolean.TRUE.equals(blocked)) {
                return true;
            }
            Thread.onSpinWait();
        }
        return false;
    }

    private void createSuccessorFailureTrigger(String originalTokenHash) {
        jdbcTemplate.execute("""
                create function reject_refresh_successor_insert() returns trigger
                language plpgsql as $$
                begin
                    if exists (
                        select 1
                        from refresh_tokens original
                        where original.user_id = new.user_id
                          and original.token_hash = '%s'
                          and original.revoked_at is not null
                    ) then
                        raise exception 'forced successor insert failure after original consumption';
                    end if;
                    return new;
                end;
                $$
                """.formatted(originalTokenHash));
        jdbcTemplate.execute("""
                create trigger reject_refresh_successor_insert_trigger
                before insert on refresh_tokens
                for each row execute function reject_refresh_successor_insert()
                """);
    }

    private void dropSuccessorFailureTrigger() {
        jdbcTemplate.execute("drop trigger if exists reject_refresh_successor_insert_trigger on refresh_tokens");
        jdbcTemplate.execute("drop function if exists reject_refresh_successor_insert()");
    }

    private AuthenticatedUser authenticatedUser() {
        return new AuthenticatedUser(
                USER_ID,
                "refresh-concurrency-user@example.test",
                "refresh-concurrency-user",
                "test-password-hash",
                true,
                Set.of(),
                Set.of()
        );
    }

    private void await(CountDownLatch latch, String timeoutMessage) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(timeoutMessage);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Refresh token concurrency test interrupted", exception);
        }
    }

    private void awaitExecutorTermination() {
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for refresh test threads to terminate");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while stopping refresh test threads", exception);
        }
    }

    private record RefreshAttempt(boolean successful, String refreshToken, Class<?> failureType) {
    }
}
