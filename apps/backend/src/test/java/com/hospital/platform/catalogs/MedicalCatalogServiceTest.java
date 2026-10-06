package com.hospital.platform.catalogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hospital.platform.catalogs.repository.MedicalCatalogRepository;
import com.hospital.platform.catalogs.service.InvalidMedicalCatalogQueryException;
import com.hospital.platform.catalogs.service.MedicalCatalogService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MedicalCatalogServiceTest {

    private final MedicalCatalogRepository repository = Mockito.mock(MedicalCatalogRepository.class);
    private final MedicalCatalogService service = new MedicalCatalogService(repository);

    @Test
    void normalizesQueriesAndPassesBoundedLimit() {
        when(repository.searchIcd10("fiebre", 20)).thenReturn(List.of());

        assertThat(service.searchIcd10("  FIEBRE  ", 20)).isEmpty();
        verify(repository).searchIcd10("fiebre", 20);
    }

    @Test
    void rejectsBlankShortOrOversizedQueriesBeforeAccessingRepository() {
        assertThatThrownBy(() -> service.searchIcd10(" ", 20))
                .isInstanceOf(InvalidMedicalCatalogQueryException.class);
        assertThatThrownBy(() -> service.searchMedications("x", 20))
                .isInstanceOf(InvalidMedicalCatalogQueryException.class);
        assertThatThrownBy(() -> service.searchProcedures("x".repeat(101), 20))
                .isInstanceOf(InvalidMedicalCatalogQueryException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsOutOfRangeLimitsIncludingDependentPresentations() {
        assertThatThrownBy(() -> service.searchIcd10("test", 0))
                .isInstanceOf(InvalidMedicalCatalogQueryException.class);
        assertThatThrownBy(() -> service.searchMedications("test", 51))
                .isInstanceOf(InvalidMedicalCatalogQueryException.class);
        assertThatThrownBy(() -> service.findPresentations(UUID.randomUUID(), -1))
                .isInstanceOf(InvalidMedicalCatalogQueryException.class);
        verifyNoInteractions(repository);
    }
}
