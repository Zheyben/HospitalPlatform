package com.hospital.platform.audit.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.audit.entity.AuditLog;
import com.hospital.platform.audit.repository.AuditLogRepository;
import com.hospital.platform.users.service.CurrentUserService;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class DatabaseAuditLogServiceTest {

    private static final UUID ACTOR_ID = UUID.fromString("77777777-7777-7777-7777-777777777771");
    private static final UUID ENTITY_ID = UUID.fromString("88888888-8888-8888-8888-888888888881");

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private CurrentUserService currentUserService;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new DatabaseAuditLogService(auditLogRepository, currentUserService);
    }

    @Test
    void recordsValidEventAndMapsEveryContractField() {
        Map<String, Object> oldValues = Map.of("appointmentStatus", "SCHEDULED");
        Map<String, Object> newValues = Map.of("appointmentStatus", "CONFIRMED");
        when(currentUserService.currentUserId()).thenReturn(ACTOR_ID);

        auditLogService.record(
                AuditEventType.APPOINTMENT_CONFIRMED,
                "Appointment",
                ENTITY_ID,
                oldValues,
                newValues
        );

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog auditLog = captor.getValue();
        assertThat(auditLog.getUserId()).isEqualTo(ACTOR_ID);
        assertThat(auditLog.getAction()).isEqualTo(AuditEventType.APPOINTMENT_CONFIRMED);
        assertThat(auditLog.getEntityName()).isEqualTo("Appointment");
        assertThat(auditLog.getEntityId()).isEqualTo(ENTITY_ID);
        assertThat(auditLog.getOldValues()).containsExactlyEntriesOf(oldValues);
        assertThat(auditLog.getNewValues()).containsExactlyEntriesOf(newValues);
        assertThat(auditLog.getIpAddress()).isNull();
        assertThat(auditLog.getUserAgent()).isNull();
        verify(currentUserService).currentUserId();
        verifyNoMoreInteractions(auditLogRepository, currentUserService);
    }

    @Test
    void requiresAnExistingTransactionByContractImplementation() throws Exception {
        Method record = DatabaseAuditLogService.class.getMethod(
                "record",
                AuditEventType.class,
                String.class,
                UUID.class,
                Map.class,
                Map.class
        );

        Transactional transactional = record.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.propagation()).isEqualTo(Propagation.MANDATORY);
    }

    @Test
    void publicContractDoesNotExposePersistenceTypes() {
        for (Method method : AuditLogService.class.getDeclaredMethods()) {
            assertThat(method.getReturnType().getPackageName())
                    .doesNotStartWith("com.hospital.platform.audit.entity")
                    .doesNotStartWith("com.hospital.platform.audit.repository");
            for (Class<?> parameterType : method.getParameterTypes()) {
                assertThat(parameterType.getPackageName())
                        .doesNotStartWith("com.hospital.platform.audit.entity")
                        .doesNotStartWith("com.hospital.platform.audit.repository");
            }
        }
    }
}
