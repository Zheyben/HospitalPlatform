package com.hospital.platform.appointments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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
class AppointmentPersistenceIT {

    private static final UUID PROFESSIONAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333341");
    private static final UUID SPECIALTY_ID = UUID.fromString("44444444-4444-4444-4444-444444444451");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666671");
    private static final UUID SCHEDULE_ID = UUID.fromString("11111111-1111-1111-1111-111111111131");
    private static final UUID SLOT_A_ID = UUID.fromString("22222222-2222-2222-2222-222222222241");
    private static final UUID SLOT_B_ID = UUID.fromString("22222222-2222-2222-2222-222222222242");
    private static final UUID SLOT_C_ID = UUID.fromString("22222222-2222-2222-2222-222222222243");
    private static final UUID SLOT_D_ID = UUID.fromString("22222222-2222-2222-2222-222222222244");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_appointment_persistence_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update("delete from appointments");
        jdbcTemplate.update("delete from availability_slots");
        jdbcTemplate.update("delete from schedules");
        jdbcTemplate.update("delete from patients");
        jdbcTemplate.update("delete from professionals");
        jdbcTemplate.update("delete from specialties");

        jdbcTemplate.update(
                "insert into specialties (id, name) values (?, ?)",
                SPECIALTY_ID,
                "Appointment Persistence Specialty"
        );
        jdbcTemplate.update(
                "insert into professionals (id, license_number) values (?, ?)",
                PROFESSIONAL_ID,
                "CMP-APPOINTMENT-PERSISTENCE"
        );
        jdbcTemplate.update(
                "insert into patients (id, document_type, document_number) values (?, 'DNI', ?)",
                PATIENT_ID,
                "91000001"
        );
        jdbcTemplate.update(
                "insert into schedules "
                        + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                        + "values (?, ?, ?, 1, time '09:00', time '13:00', true)",
                SCHEDULE_ID,
                PROFESSIONAL_ID,
                SPECIALTY_ID
        );

        insertSlot(SLOT_A_ID, "09:00");
        insertSlot(SLOT_B_ID, "10:00");
        insertSlot(SLOT_C_ID, "11:00");
        insertSlot(SLOT_D_ID, "12:00");
    }

    @Test
    void appliesFlywayV3AndExposesTheExpectedSchema() {
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("3");
        assertThat(columnExists("appointments", "rescheduled_from_id")).isTrue();
        assertThat(constraintExists("appointments_slot_id_key")).isFalse();
        assertThat(constraintExists("fk_appointments_rescheduled_from")).isTrue();
        assertThat(constraintExists("uq_appointments_rescheduled_from")).isTrue();
        assertThat(indexDefinition("uq_appointments_active_slot"))
                .contains("UNIQUE INDEX")
                .contains("slot_id")
                .contains("appointment_status")
                .contains("SCHEDULED")
                .contains("CONFIRMED");
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("refresh_tokens")).isTrue();
    }

    @Test
    void mapsNullableAndPresentPredecessorsAndFindsTheSuccessor() {
        UUID originalId = UUID.randomUUID();
        UUID successorId = UUID.randomUUID();
        insertAppointment(originalId, SLOT_A_ID, "RESCHEDULED", null);
        insertAppointment(successorId, SLOT_B_ID, "SCHEDULED", originalId);

        Appointment original = appointmentRepository.findById(originalId).orElseThrow();
        Appointment successor = appointmentRepository.findById(successorId).orElseThrow();

        assertThat(original.getRescheduledFromId()).isNull();
        assertThat(successor.getRescheduledFromId()).isEqualTo(originalId);
        assertThat(appointmentRepository.findByRescheduledFromId(originalId))
                .map(Appointment::getId)
                .contains(successorId);
    }

    @Test
    void allowsOneDirectSuccessorAndSupportsAReschedulingChain() {
        UUID appointmentA = UUID.randomUUID();
        UUID appointmentB = UUID.randomUUID();
        UUID appointmentC = UUID.randomUUID();

        insertAppointment(appointmentA, SLOT_A_ID, "RESCHEDULED", null);
        insertAppointment(appointmentB, SLOT_B_ID, "RESCHEDULED", appointmentA);
        insertAppointment(appointmentC, SLOT_C_ID, "SCHEDULED", appointmentB);

        assertThat(appointmentRepository.findByRescheduledFromId(appointmentA))
                .map(Appointment::getId)
                .contains(appointmentB);
        assertThat(appointmentRepository.findByRescheduledFromId(appointmentB))
                .map(Appointment::getId)
                .contains(appointmentC);

        assertThatThrownBy(() -> insertAppointment(
                UUID.randomUUID(),
                SLOT_D_ID,
                "SCHEDULED",
                appointmentA
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsMissingAndSelfPredecessorReferences() {
        assertThatThrownBy(() -> insertAppointment(
                UUID.randomUUID(),
                SLOT_A_ID,
                "SCHEDULED",
                UUID.randomUUID()
        )).isInstanceOf(DataIntegrityViolationException.class);

        UUID selfReferencedId = UUID.randomUUID();
        assertThatThrownBy(() -> insertAppointment(
                selfReferencedId,
                SLOT_B_ID,
                "SCHEDULED",
                selfReferencedId
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsTwoActiveAppointmentsForTheSameSlot() {
        insertAppointment(UUID.randomUUID(), SLOT_A_ID, "SCHEDULED", null);

        assertThatThrownBy(() -> insertAppointment(
                UUID.randomUUID(),
                SLOT_A_ID,
                "CONFIRMED",
                null
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsSlotReuseAfterCancellationAndRescheduling() {
        insertAppointment(UUID.randomUUID(), SLOT_A_ID, "CANCELLED", null);
        insertAppointment(UUID.randomUUID(), SLOT_A_ID, "SCHEDULED", null);
        insertAppointment(UUID.randomUUID(), SLOT_B_ID, "RESCHEDULED", null);
        insertAppointment(UUID.randomUUID(), SLOT_B_ID, "CONFIRMED", null);

        assertThat(appointmentCount(SLOT_A_ID)).isEqualTo(2);
        assertThat(appointmentCount(SLOT_B_ID)).isEqualTo(2);
    }

    @Test
    void obtainsAnAppointmentWithAPessimisticWriteLock() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        insertAppointment(appointmentId, SLOT_A_ID, "SCHEDULED", null);

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Void> lockHolder = executor.submit(() -> {
                transactionTemplate.executeWithoutResult(status -> {
                    appointmentRepository.findByIdForUpdate(appointmentId).orElseThrow();
                    lockAcquired.countDown();
                    await(releaseLock);
                });
                return null;
            });

            assertThat(lockAcquired.await(10, TimeUnit.SECONDS)).isTrue();

            Future<Boolean> blockedUpdate = executor.submit(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        jdbcTemplate.execute("set local lock_timeout = '500ms'");
                        jdbcTemplate.update(
                                "update appointments set reason = 'concurrent update' where id = ?",
                                appointmentId
                        );
                    });
                    return false;
                } catch (DataAccessException exception) {
                    return true;
                }
            });

            try {
                assertThat(blockedUpdate.get(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                releaseLock.countDown();
            }
            lockHolder.get(5, TimeUnit.SECONDS);
        }

        assertThat(appointmentRepository.findById(appointmentId).orElseThrow().getReason())
                .isEqualTo("Persistence integration test");
    }

    @Test
    void preservesExistingAppointmentDataWhenMigratingFromV2() {
        String schema = "appointment_v2_upgrade_test";
        JdbcTemplate migrationJdbc = new JdbcTemplate(dataSource);
        migrationJdbc.execute("drop schema if exists " + schema + " cascade");

        try {
            Flyway v2Flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .schemas(schema)
                    .defaultSchema(schema)
                    .createSchemas(true)
                    .target("2")
                    .load();
            v2Flyway.migrate();

            UUID appointmentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID refreshTokenId = UUID.randomUUID();
            insertLegacyV2Data(migrationJdbc, schema, appointmentId, userId, refreshTokenId);

            Flyway latestFlyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .schemas(schema)
                    .defaultSchema(schema)
                    .createSchemas(true)
                    .load();
            latestFlyway.migrate();

            assertThat(latestFlyway.info().current().getVersion().toString()).isEqualTo("3");
            assertThat(migrationJdbc.queryForObject(
                    "select count(*) from " + schema + ".appointments where id = ?",
                    Integer.class,
                    appointmentId
            )).isEqualTo(1);
            assertThat(migrationJdbc.queryForObject(
                    "select rescheduled_from_id from " + schema + ".appointments where id = ?",
                    UUID.class,
                    appointmentId
            )).isNull();
            assertThat(migrationJdbc.queryForObject(
                    "select count(*) from " + schema + ".users where id = ?",
                    Integer.class,
                    userId
            )).isEqualTo(1);
            assertThat(migrationJdbc.queryForObject(
                    "select count(*) from " + schema + ".refresh_tokens where id = ?",
                    Integer.class,
                    refreshTokenId
            )).isEqualTo(1);
        } finally {
            migrationJdbc.execute("drop schema if exists " + schema + " cascade");
        }
    }

    private void insertSlot(UUID slotId, String startTime) {
        jdbcTemplate.update(
                "insert into availability_slots "
                        + "(id, schedule_id, slot_date, start_time, end_time, status) "
                        + "values (?, ?, date '2030-10-02', cast(? as time), cast(? as time) + interval '30 minutes', "
                        + "'RESERVED')",
                slotId,
                SCHEDULE_ID,
                startTime,
                startTime
        );
    }

    private void insertAppointment(UUID appointmentId, UUID slotId, String status, UUID rescheduledFromId) {
        if (rescheduledFromId == null) {
            jdbcTemplate.update(
                    "insert into appointments "
                            + "(id, patient_id, professional_id, slot_id, appointment_status, reason) "
                            + "values (?, ?, ?, ?, ?, 'Persistence integration test')",
                    appointmentId,
                    PATIENT_ID,
                    PROFESSIONAL_ID,
                    slotId,
                    status
            );
            return;
        }

        jdbcTemplate.update(
                "insert into appointments "
                        + "(id, patient_id, professional_id, slot_id, appointment_status, reason, "
                        + "rescheduled_from_id) values (?, ?, ?, ?, ?, 'Persistence integration test', ?)",
                appointmentId,
                PATIENT_ID,
                PROFESSIONAL_ID,
                slotId,
                status,
                rescheduledFromId
        );
    }

    private void insertLegacyV2Data(
            JdbcTemplate migrationJdbc,
            String schema,
            UUID appointmentId,
            UUID userId,
            UUID refreshTokenId
    ) {
        UUID specialtyId = UUID.randomUUID();
        UUID professionalId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        migrationJdbc.update(
                "insert into " + schema + ".users "
                        + "(id, username, email, password_hash) values (?, ?, ?, ?)",
                userId,
                "legacy-user",
                "legacy@example.test",
                "legacy-password-hash"
        );
        migrationJdbc.update(
                "insert into " + schema + ".refresh_tokens "
                        + "(id, user_id, token_hash, expires_at) "
                        + "values (?, ?, ?, current_timestamp + interval '1 day')",
                refreshTokenId,
                userId,
                "legacy-refresh-token-hash"
        );
        migrationJdbc.update("insert into " + schema + ".specialties (id, name) values (?, ?)",
                specialtyId, "Legacy Specialty");
        migrationJdbc.update("insert into " + schema + ".professionals (id, license_number) values (?, ?)",
                professionalId, "CMP-LEGACY-V2");
        migrationJdbc.update(
                "insert into " + schema + ".patients (id, document_type, document_number) values (?, 'DNI', ?)",
                patientId,
                "92000001"
        );
        migrationJdbc.update(
                "insert into " + schema + ".schedules "
                        + "(id, professional_id, specialty_id, day_of_week, start_time, end_time, active) "
                        + "values (?, ?, ?, 1, time '09:00', time '10:00', true)",
                scheduleId,
                professionalId,
                specialtyId
        );
        migrationJdbc.update(
                "insert into " + schema + ".availability_slots "
                        + "(id, schedule_id, slot_date, start_time, end_time, status) "
                        + "values (?, ?, date '2030-10-03', time '09:00', time '09:30', 'RESERVED')",
                slotId,
                scheduleId
        );
        migrationJdbc.update(
                "insert into " + schema + ".appointments "
                        + "(id, patient_id, professional_id, slot_id, appointment_status) "
                        + "values (?, ?, ?, ?, 'SCHEDULED')",
                appointmentId,
                patientId,
                professionalId,
                slotId
        );
    }

    private boolean columnExists(String tableName, String columnName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select exists (select 1 from information_schema.columns "
                        + "where table_schema = current_schema() and table_name = ? and column_name = ?)",
                Boolean.class,
                tableName,
                columnName
        ));
    }

    private boolean constraintExists(String constraintName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select exists (select 1 from pg_constraint "
                        + "where conrelid = 'appointments'::regclass and conname = ?)",
                Boolean.class,
                constraintName
        ));
    }

    private String indexDefinition(String indexName) {
        return jdbcTemplate.queryForObject(
                "select indexdef from pg_indexes where schemaname = current_schema() and indexname = ?",
                String.class,
                indexName
        );
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select exists (select 1 from information_schema.tables "
                        + "where table_schema = current_schema() and table_name = ?)",
                Boolean.class,
                tableName
        ));
    }

    private int appointmentCount(UUID slotId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from appointments where slot_id = ?",
                Integer.class,
                slotId
        );
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for persistence test coordination");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Persistence test coordination interrupted", exception);
        }
    }
}
