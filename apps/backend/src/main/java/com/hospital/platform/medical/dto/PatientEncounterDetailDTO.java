package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PatientEncounterDetailDTO(
        UUID encounterId, LocalDate appointmentDate, Instant finalizedAt,
        String doctorName, String specialtyName,
        String reason, String symptomsAndCurrentIllness,
        String primaryDiagnosis, String icd10Code, String icd10Description,
        String diagnosisType, String therapeuticPlan, String generalIndications,
        String patientEducation, String warningSigns, LocalDate suggestedFollowUpDate,
        String followUpReason, String pendingResultsAndPlan, String complementaryObservations,
        String patientSex,
        String patientMaritalStatus,
        String patientOccupation,
        String patientDistrict,
        String patientEducationLevel,
        String patientAffiliationNumber,
        String patientEmergencyContactName,
        String patientEmergencyContactRelationship,
        String patientEmergencyContactPhone
) {
}
