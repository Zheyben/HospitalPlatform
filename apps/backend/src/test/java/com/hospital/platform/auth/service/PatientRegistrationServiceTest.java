package com.hospital.platform.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.auth.dto.RegisterPatientRequestDTO;
import com.hospital.platform.auth.dto.RegisterPatientResponseDTO;
import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.exception.DuplicateDocumentException;
import com.hospital.platform.patients.service.PatientService;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.exception.DuplicateEmailException;
import com.hospital.platform.users.service.UserService;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

@ExtendWith(MockitoExtension.class)
class PatientRegistrationServiceTest {

    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555551");
    private static final UUID PATIENT_ID = UUID.fromString("66666666-6666-6666-6666-666666666661");

    @Mock private UserService userService;
    @Mock private PatientService patientService;

    private PatientRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new PatientRegistrationService(userService, patientService);
    }

    @Test
    void registersAccountAndLinkedPatientWithoutReturningPassword() {
        RegisterPatientRequestDTO request = request();
        User user = new User(USER_ID, "patient-generated", "patient@example.com", "bcrypt-hash", true);
        user.setNames("Ana", "Pérez");
        when(userService.registerPatientUser(
                request.email(), request.password(), request.firstName(), request.lastName()
        )).thenReturn(user);
        when(patientService.registerPatient(
                USER_ID, request.documentType(), request.documentNumber(), request.birthDate(),
                request.phone(), request.insurance()
        )).thenReturn(new PatientResponseDTO(
                PATIENT_ID, USER_ID, "DNI", "12345678", request.birthDate(),
                request.phone(), request.insurance(), null, true
        ));

        RegisterPatientResponseDTO response = registrationService.register(request);

        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.patientId()).isEqualTo(PATIENT_ID);
        assertThat(response.email()).isEqualTo("patient@example.com");
        assertThat(response.insurance()).isEqualTo("Demo Health");
        assertThat(RegisterPatientResponseDTO.class.getRecordComponents())
                .extracting(component -> component.getName())
                .doesNotContain("password", "passwordHash", "roles");
        verify(patientService).registerPatient(
                USER_ID, "DNI", "12345678", request.birthDate(), "3001234567", "Demo Health"
        );
    }

    @Test
    void duplicateEmailStopsBeforeCreatingPatient() {
        RegisterPatientRequestDTO request = request();
        when(userService.registerPatientUser(
                request.email(), request.password(), request.firstName(), request.lastName()
        )).thenThrow(new DuplicateEmailException(request.email()));

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
        verifyNoInteractions(patientService);
    }

    @Test
    void patientFailureRollsBackRegistrationTransaction() {
        RegisterPatientRequestDTO request = request();
        User user = new User(USER_ID, "patient-generated", "patient@example.com", "bcrypt-hash", true);
        when(userService.registerPatientUser(
                request.email(), request.password(), request.firstName(), request.lastName()
        )).thenReturn(user);
        when(patientService.registerPatient(
                USER_ID, request.documentType(), request.documentNumber(), request.birthDate(),
                request.phone(), request.insurance()
        )).thenThrow(new DuplicateDocumentException(request.documentNumber()));

        RecordingTransactionManager transactionManager = new RecordingTransactionManager();
        ProxyFactory proxyFactory = new ProxyFactory(registrationService);
        proxyFactory.addAdvice(new TransactionInterceptor(
                transactionManager, new AnnotationTransactionAttributeSource()
        ));
        PatientRegistrationService transactionalService = (PatientRegistrationService) proxyFactory.getProxy();

        assertThatThrownBy(() -> transactionalService.register(request))
                .isInstanceOf(DuplicateDocumentException.class);
        assertThat(transactionManager.rolledBack.get()).isTrue();
        assertThat(transactionManager.committed.get()).isFalse();
    }

    private RegisterPatientRequestDTO request() {
        return new RegisterPatientRequestDTO(
                "patient@example.com", "plain-password", "DNI", "12345678", "Ana", "Pérez",
                LocalDate.of(1990, 1, 1), "3001234567", "Demo Health"
        );
    }

    private static class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        private final AtomicBoolean rolledBack = new AtomicBoolean();
        private final AtomicBoolean committed = new AtomicBoolean();

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            committed.set(true);
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rolledBack.set(true);
        }
    }
}
