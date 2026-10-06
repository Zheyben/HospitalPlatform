package com.hospital.platform.medical.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MedicalPrescriptionDraftUpdateDTO(
        @NotNull @Min(0) Integer version,
        @NotNull @Valid MedicalPrescriptionDraftDTO prescription
) {
    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("Unknown prescription update field");
    }
}
