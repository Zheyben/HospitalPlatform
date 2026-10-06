package com.hospital.platform.patients.dto;

import java.util.UUID;

public record ReceptionPatientSearchDTO(
        UUID patientId,
        String documentType,
        String documentNumber,
        String patientName
) {
}
