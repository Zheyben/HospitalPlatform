package com.hospital.platform.medical.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MedicalHistoryDraftUpdateDTO(
        @NotNull @Min(0) Integer version,
        @NotNull @Valid MedicalHistoryDraftDTO history
) {
    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("Unknown draft field");
    }
}
