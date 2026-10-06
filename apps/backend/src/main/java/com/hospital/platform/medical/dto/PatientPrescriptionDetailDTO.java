package com.hospital.platform.medical.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PatientPrescriptionDetailDTO(
        UUID prescriptionId, UUID encounterId, String prescriptionNumber, Instant issuedAt,
        String patientName, String patientDocumentType, String patientDocumentNumber,
        String doctorName, String specialtyName, String primaryDiagnosis,
        String icd10Code, String additionalPrecautions,
        String nonPharmacologicalRecommendations, String warningSigns,
        String additionalCare, String followUpObservations, List<MedicationItem> items
) {
    public record MedicationItem(
            int itemNumber, String genericName, String commercialName,
            String presentation, String concentration, String pharmaceuticalForm,
            BigDecimal dose, String doseUnit, String frequency, String route,
            BigDecimal duration, String durationUnit, int quantity, String usageInstructions
    ) {
    }
}
