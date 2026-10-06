package com.hospital.platform.medical.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MedicalAssessmentDraftDTO(
        @Valid Presentation presentation,
        @Valid VitalSigns vitalSigns,
        @Valid Examination examination,
        @Valid Diagnosis diagnosis,
        @Valid TreatmentPlan treatmentPlan
) {
    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("Unknown assessment field");
    }

    public record Presentation(
            @Size(max = 4000) String reason,
            @Size(max = 4000) String symptomsAndCurrentIllness,
            @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 3)
            BigDecimal illnessDuration,
            DurationUnit illnessDurationUnit,
            @Size(max = 4000) String biologicalFunctions,
            @Size(max = 4000) String reviewedHistoryAndAllergies,
            @Size(max = 4000) String priorTreatmentAndResponse
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown presentation field");
        }
    }

    public record VitalSigns(
            @Min(1) Integer systolicBloodPressure,
            @Min(1) Integer diastolicBloodPressure,
            @Min(1) Integer heartRate,
            @Min(1) Integer respiratoryRate,
            @DecimalMin(value = "0", inclusive = false) @Digits(integer = 4, fraction = 2)
            BigDecimal temperatureCelsius,
            @Min(1) @Max(100) Integer oxygenSaturationPercent,
            @DecimalMin(value = "0", inclusive = false) @Digits(integer = 5, fraction = 3)
            BigDecimal weightKg,
            @DecimalMin(value = "0", inclusive = false) @Digits(integer = 5, fraction = 3)
            BigDecimal heightCm
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown vital sign field");
        }
    }

    public record Examination(
            @Size(max = 4000) String generalCondition,
            @Size(max = 4000) String headAndNeck,
            @Size(max = 4000) String cardiopulmonaryAndAbdomen,
            @Size(max = 4000) String extremitiesAndNeurologic,
            @Size(max = 4000) String otherFindings
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown examination field");
        }
    }

    public record Diagnosis(
            @Size(max = 4000) String primaryDiagnosis,
            UUID icd10CodeId,
            DiagnosisType diagnosisType,
            @Size(max = 4000) String observations,
            UUID procedureId,
            Priority priority
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown diagnosis field");
        }
    }

    public record TreatmentPlan(
            @Size(max = 4000) String therapeuticPlan,
            @Size(max = 4000) String generalIndications,
            @Size(max = 4000) String patientEducation,
            @Size(max = 4000) String warningSigns,
            ReferralType referralType,
            UUID referralSpecialtyId,
            LocalDate suggestedFollowUpDate,
            @Size(max = 4000) String followUpReason,
            @Size(max = 4000) String pendingResultsAndPlan,
            @Size(max = 4000) String complementaryObservations
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown treatment plan field");
        }
    }

    public enum DurationUnit { HOURS, DAYS, WEEKS, MONTHS }
    public enum DiagnosisType { PRESUMPTIVE, DEFINITIVE }
    public enum Priority { ROUTINE, PREFERRED, URGENT }
    public enum ReferralType { NONE, INTERCONSULTATION }
}
