package com.hospital.platform.medical.dto;

import java.util.List;
import java.util.UUID;

public record MedicalProfessionalContextDTO(
        UUID professionalId,
        String firstName,
        String lastName,
        String licenseNumber,
        String simulatedRne,
        UUID specialtyId,
        String specialtyName,
        List<MedicalReadyAppointmentDTO> readyAppointments
) {
}
