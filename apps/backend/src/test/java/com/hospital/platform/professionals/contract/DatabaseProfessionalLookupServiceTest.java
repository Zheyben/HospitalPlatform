package com.hospital.platform.professionals.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.hospital.platform.professionals.entity.Professional;
import com.hospital.platform.professionals.repository.ProfessionalRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseProfessionalLookupServiceTest {

    private static final UUID PROFESSIONAL_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID USER_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @Mock
    private ProfessionalRepository professionalRepository;

    private ProfessionalLookupService professionalLookupService;

    @BeforeEach
    void setUp() {
        professionalLookupService = new DatabaseProfessionalLookupService(professionalRepository);
    }

    @Test
    void detectsActiveProfessional() {
        when(professionalRepository.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID))
                .thenReturn(Optional.of(new Professional(PROFESSIONAL_ID, null, "CMP-123")));

        assertThat(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).isTrue();
    }

    @Test
    void rejectsInactiveOrMissingProfessional() {
        when(professionalRepository.findByIdAndDeletedAtIsNull(PROFESSIONAL_ID)).thenReturn(Optional.empty());

        assertThat(professionalLookupService.existsActiveProfessional(PROFESSIONAL_ID)).isFalse();
    }

    @Test
    void validatesActiveProfessionalOwnershipWithoutExposingEntity() {
        when(professionalRepository.existsByIdAndUserIdAndDeletedAtIsNull(PROFESSIONAL_ID, USER_ID))
                .thenReturn(true);

        assertThat(professionalLookupService.isActiveProfessionalLinkedToUser(PROFESSIONAL_ID, USER_ID))
                .isTrue();
    }

    @Test
    void rejectsMissingInactiveOrDifferentlyLinkedProfessional() {
        when(professionalRepository.existsByIdAndUserIdAndDeletedAtIsNull(PROFESSIONAL_ID, USER_ID))
                .thenReturn(false);

        assertThat(professionalLookupService.isActiveProfessionalLinkedToUser(PROFESSIONAL_ID, USER_ID))
                .isFalse();
    }
}
