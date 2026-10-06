package com.hospital.platform.medical.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MedicalPrescriptionDraftDTO(
        @Size(max = 4000) String additionalPrecautions,
        @Size(max = 4000) String nonPharmacologicalRecommendations,
        @Size(max = 4000) String warningSigns,
        @Size(max = 4000) String additionalCare,
        @Size(max = 4000) String followUpObservations,
        @NotNull @Size(max = 50) List<@NotNull @Valid MedicationItem> items
) {
    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("Unknown prescription field");
    }

    public record MedicationItem(
            @NotNull UUID medicationId,
            @NotNull UUID presentationId,
            @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 3) BigDecimal dose,
            @NotNull DoseUnit doseUnit,
            @NotNull Frequency frequency,
            @NotNull Route route,
            @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 3) BigDecimal duration,
            @NotNull DurationUnit durationUnit,
            @NotNull @Min(1) Integer quantity,
            @NotBlank @Size(max = 4000) String usageInstructions
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown medication item field");
        }
    }

    public enum DoseUnit { MG, G, ML, DROPS, TABLET, CAPSULE, IU }
    public enum Frequency { EVERY_6_HOURS, EVERY_8_HOURS, EVERY_12_HOURS, EVERY_24_HOURS, ONCE }
    public enum Route { ORAL, SUBLINGUAL, INTRAMUSCULAR, INTRAVENOUS, SUBCUTANEOUS, TOPICAL,
        INHALATION, OPHTHALMIC, OTIC, RECTAL, VAGINAL }
    public enum DurationUnit { DAYS, WEEKS }
}
