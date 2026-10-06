package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MedicalPriorEncounterDTO(
        UUID encounterId, LocalDate appointmentDate, Instant finalizedAt,
        String simulatedService, String doctorName, String specialtyName,
        String reason, String primaryDiagnosis, String icd10Code
) {
}
