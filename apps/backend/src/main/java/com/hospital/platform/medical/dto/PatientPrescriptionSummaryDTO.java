package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.util.UUID;

public record PatientPrescriptionSummaryDTO(UUID prescriptionId, UUID encounterId,
                                            String prescriptionNumber, Instant issuedAt,
                                            String doctorName, String specialtyName,
                                            String primaryDiagnosis, String icd10Code) {
}
