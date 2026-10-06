package com.hospital.platform.medical.dto;

import jakarta.validation.constraints.Min;

public record MedicalFinalizationRequestDTO(@Min(1) int version) {
}
