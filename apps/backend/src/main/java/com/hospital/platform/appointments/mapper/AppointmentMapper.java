package com.hospital.platform.appointments.mapper;

import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.entity.Appointment;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDTO toResponse(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getProfessionalId(),
                appointment.getSlotId(),
                appointment.getAppointmentStatus(),
                appointment.getFlowStage(),
                appointment.getReason(),
                appointment.getCancelledAt(),
                appointment.getCancelledBy(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    public List<AppointmentResponseDTO> toResponseList(List<Appointment> appointments) {
        return appointments.stream().map(this::toResponse).toList();
    }
}
