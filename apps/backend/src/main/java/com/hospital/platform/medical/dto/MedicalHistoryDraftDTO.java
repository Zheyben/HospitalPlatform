package com.hospital.platform.medical.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MedicalHistoryDraftDTO(
        @Valid Personal personal,
        @Valid Family family,
        @Valid Gynecologic gynecologic,
        @Size(max = 4000) String otherAlerts
) {
    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("Unknown clinical history field");
    }

    public record Personal(
            @Size(max = 4000) String pathological,
            @Size(max = 4000) String surgical,
            @Size(max = 4000) String allergiesAndReactions,
            @Size(max = 4000) String usualMedication,
            @Size(max = 4000) String transfusions,
            @Size(max = 4000) String relevantHabits,
            @Size(max = 4000) String hospitalizations,
            @Size(max = 4000) String other
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown personal history field");
        }
    }

    public record Family(
            @Size(max = 4000) String father,
            @Size(max = 4000) String mother,
            @Size(max = 4000) String siblings,
            @Size(max = 4000) String children,
            @Size(max = 4000) String grandparents,
            @Size(max = 4000) String other,
            @Size(max = 250) String source,
            LocalDate sourceDate,
            @Size(max = 4000) String observation
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown family history field");
        }
    }

    public record Gynecologic(
            @Min(0) Integer pregnancies,
            @Min(0) Integer births,
            @Min(0) Integer miscarriages,
            @Min(0) Integer cesareans,
            LocalDate lastMenstrualPeriod,
            @Size(max = 4000) String other
    ) {
        @JsonAnySetter
        public void rejectUnknown(String name, Object value) {
            throw new IllegalArgumentException("Unknown gynecologic history field");
        }
    }
}
