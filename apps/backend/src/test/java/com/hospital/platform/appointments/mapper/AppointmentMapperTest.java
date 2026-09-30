package com.hospital.platform.appointments.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AppointmentMapperTest {

    @Test
    void mapsAppointmentWithoutExposingAnEntity() {
        UUID patientId = UUID.randomUUID();
        UUID professionalId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        Appointment appointment = new Appointment(null, patientId, professionalId, slotId, "Control");
        ReflectionTestUtils.invokeMethod(appointment, "prePersist");

        AppointmentResponseDTO response = new AppointmentMapper().toResponse(appointment);

        assertThat(response.id()).isNotNull();
        assertThat(response.patientId()).isEqualTo(patientId);
        assertThat(response.professionalId()).isEqualTo(professionalId);
        assertThat(response.slotId()).isEqualTo(slotId);
        assertThat(response.appointmentStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.flowStage()).isNull();
    }
}
