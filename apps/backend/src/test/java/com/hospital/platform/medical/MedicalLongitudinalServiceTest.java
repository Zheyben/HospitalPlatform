package com.hospital.platform.medical;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hospital.platform.medical.dto.MedicalAppointmentContextDTO;
import com.hospital.platform.medical.dto.PatientEncounterDetailDTO;
import com.hospital.platform.medical.repository.MedicalLongitudinalRepository;
import com.hospital.platform.medical.repository.PatientClinicalReadRepository;
import com.hospital.platform.medical.service.MedicalAppointmentNotFoundException;
import com.hospital.platform.medical.service.MedicalContextService;
import com.hospital.platform.medical.service.MedicalLongitudinalService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MedicalLongitudinalServiceTest {

    private final MedicalContextService context = mock(MedicalContextService.class);
    private final MedicalLongitudinalRepository records = mock(MedicalLongitudinalRepository.class);
    private final PatientClinicalReadRepository patientRecords = mock(PatientClinicalReadRepository.class);
    private final MedicalLongitudinalService service = new MedicalLongitudinalService(context, records, patientRecords);
    private final UUID appointmentId = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();
    private final UUID encounterId = UUID.randomUUID();

    @Test
    void inaccessibleOrNonPriorEncounterHasUniformNotFound() {
        MedicalAppointmentContextDTO current = mock(MedicalAppointmentContextDTO.class);
        when(current.patientId()).thenReturn(patientId);
        when(context.appointmentContext(appointmentId)).thenReturn(current);

        assertThatThrownBy(() -> service.detail(appointmentId, encounterId))
                .isInstanceOf(MedicalAppointmentNotFoundException.class);
        verify(records).belongsToPriorFinalizedAppointment(appointmentId, patientId, encounterId);
    }

    @Test
    void authorizedPriorEncounterMayHaveNoOrderOrPrescription() {
        MedicalAppointmentContextDTO current = mock(MedicalAppointmentContextDTO.class);
        PatientEncounterDetailDTO detail = mock(PatientEncounterDetailDTO.class);
        when(current.patientId()).thenReturn(patientId);
        when(context.appointmentContext(appointmentId)).thenReturn(current);
        when(records.belongsToPriorFinalizedAppointment(appointmentId, patientId, encounterId)).thenReturn(true);
        when(patientRecords.encounter(patientId, encounterId)).thenReturn(Optional.of(detail));

        var result = service.detail(appointmentId, encounterId);
        assertThat(result.encounter()).isSameAs(detail);
        assertThat(result.order()).isNull();
        assertThat(result.prescription()).isNull();
    }
}
