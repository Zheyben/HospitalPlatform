package com.hospital.platform.professionals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.professionals.dto.CreateProfessionalRequestDTO;
import com.hospital.platform.professionals.dto.ProfessionalResponseDTO;
import com.hospital.platform.professionals.dto.UpdateProfessionalRequestDTO;
import com.hospital.platform.professionals.entity.Professional;
import com.hospital.platform.professionals.exception.DuplicateProfessionalException;
import com.hospital.platform.professionals.exception.ProfessionalDomainConflictException;
import com.hospital.platform.professionals.repository.ProfessionalManagementRepository;
import com.hospital.platform.professionals.repository.ProfessionalRepository;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.repository.UserRepository;
import com.hospital.platform.users.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    private static final UUID PROFESSIONAL_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID SPECIALTY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_SPECIALTY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock ProfessionalRepository professionals;
    @Mock ProfessionalManagementRepository management;
    @Mock UserRepository users;
    @Mock UserService userService;
    @Mock CapacityGateway capacityGateway;

    private ProfessionalService service;

    @BeforeEach
    void setUp() {
        service = new ProfessionalService(professionals, management, users, userService, capacityGateway);
    }

    @Test
    void createsAccountAndExactlyOneSpecialtyInOneServiceOperation() {
        User user = new User(USER_ID, "professional-test", "doctor@example.com", "hash", true);
        when(management.isActiveSpecialty(SPECIALTY_ID)).thenReturn(true);
        when(userService.registerProfessionalUser("doctor@example.com", "password123", "Ana", "Ruiz"))
                .thenReturn(user);
        when(professionals.saveAndFlush(any(Professional.class)))
                .thenReturn(new Professional(PROFESSIONAL_ID, USER_ID, "01234"));
        when(management.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(response(true, SPECIALTY_ID)));

        ProfessionalResponseDTO result = service.createProfessional(createRequest());

        assertThat(result.firstName()).isEqualTo("Ana");
        assertThat(result.specialtyId()).isEqualTo(SPECIALTY_ID);
        verify(management).assignSpecialty(PROFESSIONAL_ID, SPECIALTY_ID);
    }

    @Test
    void rejectsDuplicateLicenseBeforeCreatingAccount() {
        when(professionals.existsByLicenseNumberIgnoreCase("01234")).thenReturn(true);

        assertThatThrownBy(() -> service.createProfessional(createRequest()))
                .isInstanceOf(DuplicateProfessionalException.class);
        verify(userService, never()).registerProfessionalUser(any(), any(), any(), any());
    }

    @Test
    void listsInactiveAndActiveProfessionalsForAdmin() {
        when(management.findAll()).thenReturn(List.of(response(true, SPECIALTY_ID), response(false, SPECIALTY_ID)));

        assertThat(service.findProfessionals()).hasSize(2);
    }

    @Test
    void updatesNameAndLicenseWithoutChangingSpecialty() {
        Professional professional = professional();
        User user = new User(USER_ID, "professional-test", "doctor@example.com", "hash", true);
        when(professionals.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));
        when(users.findActiveByIdWithRoles(USER_ID)).thenReturn(Optional.of(user));
        when(management.isActiveSpecialty(SPECIALTY_ID)).thenReturn(true);
        when(management.assignedSpecialty(PROFESSIONAL_ID)).thenReturn(Optional.of(SPECIALTY_ID));
        when(management.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(response(true, SPECIALTY_ID)));

        service.updateProfessional(PROFESSIONAL_ID, new UpdateProfessionalRequestDTO(
                "Ana", "Ruiz", "04567", SPECIALTY_ID));

        assertThat(user.getFirstName()).isEqualTo("Ana");
        assertThat(professional.getLicenseNumber()).isEqualTo("04567");
        verify(capacityGateway).lockUser(USER_ID);
        verify(management, never()).replaceSpecialty(any(), any());
    }

    @Test
    void rejectsSpecialtyChangeWithHistoricalSchedule() {
        when(professionals.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID))
                .thenReturn(Optional.of(professional()));
        when(users.findActiveByIdWithRoles(USER_ID))
                .thenReturn(Optional.of(new User(USER_ID, "professional-test", "doctor@example.com", "hash", true)));
        when(management.isActiveSpecialty(OTHER_SPECIALTY_ID)).thenReturn(true);
        when(management.assignedSpecialty(PROFESSIONAL_ID)).thenReturn(Optional.of(SPECIALTY_ID));
        when(management.hasSchedules(PROFESSIONAL_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateProfessional(PROFESSIONAL_ID,
                new UpdateProfessionalRequestDTO("Ana", "Ruiz", "01234", OTHER_SPECIALTY_ID)))
                .isInstanceOf(ProfessionalDomainConflictException.class);
        verify(management, never()).replaceSpecialty(any(), any());
    }

    @Test
    void changesStatusThroughCapacityGateway() {
        when(professionals.existsById(PROFESSIONAL_ID)).thenReturn(true);
        when(management.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(response(false, SPECIALTY_ID)));

        assertThat(service.changeStatus(PROFESSIONAL_ID, false).active()).isFalse();
        verify(capacityGateway).deactivateProfessional(PROFESSIONAL_ID);
    }

    @Test
    void reactivatesProfessionalThroughCapacityGateway() {
        when(professionals.existsById(PROFESSIONAL_ID)).thenReturn(true);
        when(management.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(response(true, SPECIALTY_ID)));

        assertThat(service.changeStatus(PROFESSIONAL_ID, true).active()).isTrue();
        verify(capacityGateway).reactivateProfessional(PROFESSIONAL_ID);
    }

    private CreateProfessionalRequestDTO createRequest() {
        return new CreateProfessionalRequestDTO("Ana", "Ruiz", "doctor@example.com",
                "password123", "01234", SPECIALTY_ID);
    }

    private Professional professional() {
        return new Professional(PROFESSIONAL_ID, USER_ID, "01234");
    }

    private ProfessionalResponseDTO response(boolean active, UUID specialtyId) {
        return new ProfessionalResponseDTO(PROFESSIONAL_ID, USER_ID, "Ana", "Ruiz", "01234",
                specialtyId, "Medicina General", active);
    }
}
