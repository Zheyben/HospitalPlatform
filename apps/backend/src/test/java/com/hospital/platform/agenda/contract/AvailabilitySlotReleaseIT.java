package com.hospital.platform.agenda.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class AvailabilitySlotReleaseIT {

    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333351");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444461");
    private static final UUID SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111141");
    private static final UUID RESERVED_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222251");
    private static final UUID AVAILABLE_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222252");
    private static final UUID BLOCKED_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222253");
    private static final UUID ROLLBACK_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222254");
    private static final UUID CONCURRENT_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222255");
    private static final UUID MISSING_SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222259");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_slot_release_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AvailabilitySlotReleaseService releaseService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update("delete from appointments");
        jdbcTemplate.update("delete from availability_slots");
        jdbcTemplate.update("delete from schedules");
        jdbcTemplate.update("delete from professionals");
        jdbcTemplate.update("delete from specialties");

        jdbcTemplate.update(
                "insert into specialties (id, name) values (?, ?)",
                SPECIALTY_ID,
                "Slot Release Specialty"
        );
        jdbcTemplate.update(
                "insert into professionals (id, license_number) values (?, ?)",
                PROFESSIONAL_ID,
                "CMP-SLOT-RELEASE"
        );
        jdbcTemplate.update(
                "insert into schedules "
                        + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                        + "values (?, ?, ?, 1, time '09:00', time '14:00', true)",
                SCHEDULE_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID
        );

        insertSlot(RESERVED_SLOT_ID, "09:00", "RESERVED");
        insertSlot(AVAILABLE_SLOT_ID, "10:00", "AVAILABLE");
        insertSlot(BLOCKED_SLOT_ID, "11:00", "BLOCKED");
        insertSlot(ROLLBACK_SLOT_ID, "12:00", "RESERVED");
        insertSlot(CONCURRENT_SLOT_ID, "13:00", "RESERVED");
    }

    @Test
    void releasesReservedSlotAndUpdatesTimestamp() {
        LocalDateTime previousUpdatedAt = updatedAt(RESERVED_SLOT_ID);

        releaseInTransaction(RESERVED_SLOT_ID);

        assertThat(status(RESERVED_SLOT_ID)).isEqualTo("AVAILABLE");
        assertThat(updatedAt(RESERVED_SLOT_ID)).isAfter(previousUpdatedAt);
    }

    @Test
    void rejectsAvailableSlotWithoutChangingItsState() {
        assertThatThrownBy(() -> releaseInTransaction(AVAILABLE_SLOT_ID))
                .isInstanceOf(SlotReleaseRejectedException.class);

        assertThat(status(AVAILABLE_SLOT_ID)).isEqualTo("AVAILABLE");
    }

    @Test
    void rejectsBlockedSlotWithoutChangingItsState() {
        assertThatThrownBy(() -> releaseInTransaction(BLOCKED_SLOT_ID))
                .isInstanceOf(SlotReleaseRejectedException.class);

        assertThat(status(BLOCKED_SLOT_ID)).isEqualTo("BLOCKED");
    }

    @Test
    void rejectsMissingSlot() {
        assertThatThrownBy(() -> releaseInTransaction(MISSING_SLOT_ID))
                .isInstanceOf(SlotReleaseRejectedException.class);
    }

    @Test
    void requiresAnExternalTransaction() {
        assertThatThrownBy(() -> releaseService.releaseReservedSlot(RESERVED_SLOT_ID))
                .isInstanceOf(IllegalTransactionStateException.class);

        assertThat(status(RESERVED_SLOT_ID)).isEqualTo("RESERVED");
    }

    @Test
    void externalRollbackRestoresReservedState() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            releaseService.releaseReservedSlot(ROLLBACK_SLOT_ID);
            assertThat(this.status(ROLLBACK_SLOT_ID)).isEqualTo("AVAILABLE");
            throw new ForcedRollbackException();
        })).isInstanceOf(ForcedRollbackException.class);

        assertThat(status(ROLLBACK_SLOT_ID)).isEqualTo("RESERVED");
    }

    @Test
    void allowsOnlyOneConcurrentRelease() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<String> first = executor.submit(() -> attemptRelease(ready, start));
            Future<String> second = executor.submit(() -> attemptRelease(ready, start));

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<String> results = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS)
            );
            assertThat(results).containsExactlyInAnyOrder("RELEASED", "REJECTED");
        }

        assertThat(status(CONCURRENT_SLOT_ID)).isEqualTo("AVAILABLE");
    }

    private String attemptRelease(CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        await(start);
        try {
            releaseInTransaction(CONCURRENT_SLOT_ID);
            return "RELEASED";
        } catch (SlotReleaseRejectedException exception) {
            return "REJECTED";
        }
    }

    private void releaseInTransaction(UUID slotId) {
        new TransactionTemplate(transactionManager).executeWithoutResult(
                status -> releaseService.releaseReservedSlot(slotId)
        );
    }

    private void insertSlot(UUID slotId, String startTime, String status) {
        jdbcTemplate.update(
                "insert into availability_slots "
                        + "(id, schedule_id, slot_date, start_time, end_time, status, updated_at) "
                        + "values (?, ?, date '2030-10-04', cast(? as time), "
                        + "cast(? as time) + interval '30 minutes', ?, timestamp '2020-01-01 00:00:00')",
                slotId,
                SCHEDULE_ID,
                startTime,
                startTime,
                status
        );
    }

    private String status(UUID slotId) {
        return jdbcTemplate.queryForObject(
                "select status from availability_slots where id = ?",
                String.class,
                slotId
        );
    }

    private LocalDateTime updatedAt(UUID slotId) {
        return jdbcTemplate.queryForObject(
                "select updated_at from availability_slots where id = ?",
                LocalDateTime.class,
                slotId
        );
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent slot releases");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent slot releases", exception);
        }
    }

    private static final class ForcedRollbackException extends RuntimeException {
    }
}
