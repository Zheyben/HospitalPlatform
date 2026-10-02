package com.hospital.platform.agenda.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class CapacityGatewayIT {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_capacity_test").withUsername("hospital_test").withPassword("hospital_test");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("hospital.security.jwt-secret", () -> "12345678901234567890123456789012");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired CapacityGateway capacity;
    @Autowired PlatformTransactionManager transactionManager;

    private UUID professional;
    private UUID specialty;
    private UUID schedule;
    private UUID patient;
    private LocalDate date;
    private TransactionTemplate tx;

    @BeforeEach void fixture() {
        tx = new TransactionTemplate(transactionManager);
        jdbc.execute("truncate table appointments, availability_slots, schedules, professional_specialties, patients, professionals, specialties restart identity cascade");
        professional = UUID.randomUUID();
        specialty = UUID.randomUUID();
        patient = UUID.randomUUID();
        date = LocalDate.now(ZoneId.of("America/Lima")).plusDays(7);
        jdbc.update("insert into specialties(id,name) values(?,?)", specialty, "Capacity test");
        jdbc.update("insert into professionals(id,license_number) values(?,?)", professional, "990001");
        jdbc.update("insert into professional_specialties(professional_id,specialty_id) values(?,?)", professional, specialty);
        jdbc.update("insert into patients(id,document_type,document_number) values(?,'DNI','99000001')", patient);
        schedule = tx.execute(status -> capacity.createSchedule(professional, specialty,
                date.getDayOfWeek().getValue() % 7, LocalTime.of(9, 0), LocalTime.of(11, 0)));
    }

    @Test void reserveCancelAndReserveAgainUseOneCoherentSlot() {
        UUID slot = slot(9, 0);
        UUID first = tx.execute(status -> capacity.reserve(slot, patient, "First"));
        assertThat(appointmentStatus(first)).isEqualTo("SCHEDULED");
        assertThat(slotStatus(slot)).isEqualTo("RESERVED");
        tx.executeWithoutResult(status -> capacity.cancel(first, null));
        assertThat(appointmentStatus(first)).isEqualTo("CANCELLED");
        assertThat(slotStatus(slot)).isEqualTo("AVAILABLE");
        UUID second = tx.execute(status -> capacity.reserve(slot, patient, "Second"));
        assertThat(second).isNotEqualTo(first);
        assertThat(slotStatus(slot)).isEqualTo("RESERVED");
    }

    @Test void sameSlotCannotBeReservedTwice() {
        UUID slot = slot(9, 0);
        tx.execute(status -> capacity.reserve(slot, patient, "First"));
        assertThatThrownBy(() -> tx.execute(status -> capacity.reserve(slot, patient, "Second")))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("SLOT_UNAVAILABLE");
        assertThat(jdbc.queryForObject("select count(*) from appointments where slot_id=?", Integer.class, slot))
                .isEqualTo(1);
    }

    @Test void generationIsIdempotentAndRejectsOutOfScheduleInterval() {
        boolean first = Boolean.TRUE.equals(tx.execute(status -> capacity.generateSlot(schedule, date,
                LocalTime.of(9, 0), LocalTime.of(9, 30))));
        boolean second = Boolean.TRUE.equals(tx.execute(status -> capacity.generateSlot(schedule, date,
                LocalTime.of(9, 0), LocalTime.of(9, 30))));
        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThatThrownBy(() -> tx.execute(status -> capacity.generateSlot(schedule, date,
                LocalTime.of(10, 45), LocalTime.of(11, 15))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void civilScheduleAndSlotTimesRemainInAmericaLima() {
        assertThat(jdbc.queryForObject("select start_time::text from schedules where id=?", String.class, schedule))
                .isEqualTo("09:00:00");
        assertThat(jdbc.queryForObject("select end_time::text from schedules where id=?", String.class, schedule))
                .isEqualTo("11:00:00");
        UUID slot = slot(9, 0);
        assertThat(jdbc.queryForObject("select start_time::text from availability_slots where id=?",
                String.class, slot)).isEqualTo("09:00:00");
        assertThat(jdbc.queryForObject("select end_time::text from availability_slots where id=?",
                String.class, slot)).isEqualTo("09:30:00");
    }

    @Test void deactivatedScheduleRetainsReservedCapacity() {
        UUID slot = slot(9, 0);
        tx.execute(status -> capacity.reserve(slot, patient, "Future"));
        tx.executeWithoutResult(status -> capacity.scheduleStatus(schedule, false));
        assertThat(jdbc.queryForObject("select capacity_protected from schedules where id=?",
                Boolean.class, schedule)).isTrue();
        assertThatThrownBy(() -> tx.execute(status -> capacity.createSchedule(professional, specialty,
                date.getDayOfWeek().getValue() % 7, LocalTime.of(9, 0), LocalTime.of(10, 0))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test void simultaneousReservationsYieldOneWinnerWithoutDeadlock() throws Exception {
        UUID slot = slot(9, 0);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var one = executor.submit(() -> reserveAfter(start, slot));
            var two = executor.submit(() -> reserveAfter(start, slot));
            start.countDown();
            boolean a = one.get(10, TimeUnit.SECONDS);
            boolean b = two.get(10, TimeUnit.SECONDS);
            assertThat(a ^ b).isTrue();
        }
        assertThat(jdbc.queryForObject("select count(*) from appointments where slot_id=?", Integer.class, slot))
                .isEqualTo(1);
    }

    private boolean reserveAfter(CountDownLatch start, UUID slot) {
        try {
            start.await(5, TimeUnit.SECONDS);
            tx.execute(status -> capacity.reserve(slot, patient, "Race"));
            return true;
        } catch (DataIntegrityViolationException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private UUID slot(int hour, int minute) {
        LocalTime start = LocalTime.of(hour, minute);
        tx.execute(status -> capacity.generateSlot(schedule, date, start, start.plusMinutes(30)));
        return jdbc.queryForObject("select id from availability_slots where schedule_id=? and slot_date=? "
                        + "and start_time=cast(? as time)",
                UUID.class, schedule, date, start.toString());
    }
    private String slotStatus(UUID slot) {
        return jdbc.queryForObject("select status from availability_slots where id=?", String.class, slot);
    }
    private String appointmentStatus(UUID id) {
        return jdbc.queryForObject("select appointment_status from appointments where id=?", String.class, id);
    }
}
