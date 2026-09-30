package com.hospital.platform.audit.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.platform.users.service.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
class AuditLogServiceIT {

    private static final UUID ACTOR_ID = UUID.fromString("77777777-7777-7777-7777-777777777772");
    private static final UUID ENTITY_ID = UUID.fromString("88888888-8888-8888-8888-888888888882");

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("hospital_platform_audit_test")
            .withUsername("hospital_app_test")
            .withPassword("hospital_app_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update("delete from audit_logs");
        jdbcTemplate.update("delete from users where id = ?", ACTOR_ID);
        jdbcTemplate.update(
                "insert into users (id, username, email, password_hash) values (?, ?, ?, ?)",
                ACTOR_ID,
                "audit-actor",
                "audit-actor@example.test",
                "test-password-hash"
        );
        authenticateActor();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void persistsEventWithActorJsonValuesUuidAndTimestamp() throws Exception {
        Map<String, Object> oldValues = Map.of("appointmentStatus", "SCHEDULED");
        Map<String, Object> newValues = Map.of("appointmentStatus", "CONFIRMED", "confirmed", true);
        LocalDateTime startedAt = LocalDateTime.now().minusSeconds(1);

        recordInTransaction(oldValues, newValues);

        AuditRow row = findOnlyAuditRow();
        assertThat(row.id()).isNotNull();
        assertThat(row.userId()).isEqualTo(ACTOR_ID);
        assertThat(row.action()).isEqualTo("APPOINTMENT_CONFIRMED");
        assertThat(row.entityName()).isEqualTo("Appointment");
        assertThat(row.entityId()).isEqualTo(ENTITY_ID);
        assertThat(json(row.oldValues())).isEqualTo(objectMapper.valueToTree(oldValues));
        assertThat(json(row.newValues())).isEqualTo(objectMapper.valueToTree(newValues));
        assertThat(row.ipAddress()).isNull();
        assertThat(row.userAgent()).isNull();
        assertThat(row.createdAt()).isAfter(startedAt);
    }

    @Test
    void rejectsInvocationWithoutExternalTransaction() {
        assertThatThrownBy(() -> auditLogService.record(
                AuditEventType.APPOINTMENT_CONFIRMED,
                "Appointment",
                ENTITY_ID,
                Map.of("appointmentStatus", "SCHEDULED"),
                Map.of("appointmentStatus", "CONFIRMED")
        )).isInstanceOf(IllegalTransactionStateException.class);

        assertThat(auditCount()).isZero();
    }

    @Test
    void externalRollbackRemovesPersistedAuditLog() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            auditLogService.record(
                    AuditEventType.APPOINTMENT_CANCELLED,
                    "Appointment",
                    ENTITY_ID,
                    Map.of("appointmentStatus", "CONFIRMED"),
                    Map.of("appointmentStatus", "CANCELLED")
            );
            entityManager.flush();
            assertThat(auditCount()).isEqualTo(1);
            throw new ForcedRollbackException();
        })).isInstanceOf(ForcedRollbackException.class);

        assertThat(auditCount()).isZero();
    }

    private void recordInTransaction(Map<String, Object> oldValues, Map<String, Object> newValues) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> auditLogService.record(
                AuditEventType.APPOINTMENT_CONFIRMED,
                "Appointment",
                ENTITY_ID,
                oldValues,
                newValues
        ));
    }

    private AuditRow findOnlyAuditRow() {
        return jdbcTemplate.queryForObject(
                "select id, user_id, action, entity_name, entity_id, old_values::text, "
                        + "new_values::text, ip_address, user_agent, created_at from audit_logs",
                (resultSet, rowNumber) -> new AuditRow(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getObject("user_id", UUID.class),
                        resultSet.getString("action"),
                        resultSet.getString("entity_name"),
                        resultSet.getObject("entity_id", UUID.class),
                        resultSet.getString("old_values"),
                        resultSet.getString("new_values"),
                        resultSet.getString("ip_address"),
                        resultSet.getString("user_agent"),
                        resultSet.getObject("created_at", LocalDateTime.class)
                )
        );
    }

    private int auditCount() {
        return jdbcTemplate.queryForObject("select count(*) from audit_logs", Integer.class);
    }

    private JsonNode json(String value) throws Exception {
        return objectMapper.readTree(value);
    }

    private void authenticateActor() {
        AuthenticatedUser actor = new AuthenticatedUser(
                ACTOR_ID,
                "audit-actor@example.test",
                "audit-actor",
                "test-password-hash",
                true,
                Set.of("ADMIN"),
                Set.of()
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );
    }

    private record AuditRow(
            UUID id,
            UUID userId,
            String action,
            String entityName,
            UUID entityId,
            String oldValues,
            String newValues,
            String ipAddress,
            String userAgent,
            LocalDateTime createdAt
    ) {
    }

    private static final class ForcedRollbackException extends RuntimeException {
    }
}
