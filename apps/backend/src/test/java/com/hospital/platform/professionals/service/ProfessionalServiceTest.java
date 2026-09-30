package com.hospital.platform.professionals.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.professionals.dto.CreateProfessionalRequestDTO;
import com.hospital.platform.professionals.dto.ProfessionalResponseDTO;
import com.hospital.platform.professionals.dto.UpdateProfessionalRequestDTO;
import com.hospital.platform.professionals.entity.Professional;
import com.hospital.platform.professionals.exception.DuplicateProfessionalException;
import com.hospital.platform.professionals.exception.ProfessionalNotFoundException;
import com.hospital.platform.professionals.repository.ProfessionalRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    private static final UUID PROFESSIONAL_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID USER_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    @Mock
    private ProfessionalRepository professionalRepository;

    private ProfessionalService professionalService;

    @BeforeEach
    void setUp() {
        professionalService = new ProfessionalService(professionalRepository);
    }

    @Test
    void createsProfessionalWithUniqueLicense() {
        when(professionalRepository.save(any(Professional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfessionalResponseDTO response = professionalService.createProfessional(
                new CreateProfessionalRequestDTO(USER_ID, " CMP-123 ")
        );
        ArgumentCaptor<Professional> professionalCaptor = ArgumentCaptor.forClass(Professional.class);

        verify(professionalRepository).save(professionalCaptor.capture());
        assertThat(professionalCaptor.getValue().getLicenseNumber()).isEqualTo("CMP-123");
        assertThat(response.licenseNumber()).isEqualTo("CMP-123");
        assertThat(response.userId()).isEqualTo(USER_ID);
    }

    @Test
    void rejectsDuplicatedLicense() {
        when(professionalRepository.existsByLicenseNumberIgnoreCase("CMP-123")).thenReturn(true);

        assertThatThrownBy(() -> professionalService.createProfessional(
                new CreateProfessionalRequestDTO(null, "CMP-123")
        )).isInstanceOf(DuplicateProfessionalException.class);
    }

    @Test
    void findsActiveProfessionals() {
        Professional professional = professional();
        when(professionalRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()).thenReturn(List.of(professional));

        List<ProfessionalResponseDTO> response = professionalService.findProfessionals();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(PROFESSIONAL_ID);
    }

    @Test
    void findsProfessionalById() {
        Professional professional = professional();
        when(professionalRepository.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));

        ProfessionalResponseDTO response = professionalService.findProfessionalById(PROFESSIONAL_ID);

        assertThat(response.id()).isEqualTo(PROFESSIONAL_ID);
        assertThat(response.active()).isTrue();
    }

    @Test
    void rejectsUnknownProfessional() {
        when(professionalRepository.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> professionalService.findProfessionalById(PROFESSIONAL_ID))
                .isInstanceOf(ProfessionalNotFoundException.class);
    }

    @Test
    void updatesProfessionalBasicInfo() {
        Professional professional = professional();
        when(professionalRepository.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));

        ProfessionalResponseDTO response = professionalService.updateProfessional(
                PROFESSIONAL_ID,
                new UpdateProfessionalRequestDTO("CMP-456")
        );

        assertThat(response.licenseNumber()).isEqualTo("CMP-456");
        assertThat(professional.getLicenseNumber()).isEqualTo("CMP-456");
    }

    @Test
    void rejectsDuplicatedLicenseWhenUpdating() {
        Professional professional = professional();
        when(professionalRepository.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));
        when(professionalRepository.existsByLicenseNumberIgnoreCaseAndIdNot("CMP-456", PROFESSIONAL_ID)).thenReturn(true);

        assertThatThrownBy(() -> professionalService.updateProfessional(
                PROFESSIONAL_ID,
                new UpdateProfessionalRequestDTO("CMP-456")
        )).isInstanceOf(DuplicateProfessionalException.class);
    }

    @Test
    void appliesSoftDeleteWhenProfessionalIsDeactivated() {
        Professional professional = professional();
        when(professionalRepository.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID)).thenReturn(Optional.of(professional));

        ProfessionalResponseDTO response = professionalService.deactivateProfessional(PROFESSIONAL_ID);

        assertThat(professional.getDeletedAt()).isNotNull();
        assertThat(response.active()).isFalse();
    }

    private Professional professional() {
        return new Professional(PROFESSIONAL_ID, USER_ID, "CMP-123");
    }
}
