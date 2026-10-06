package com.hospital.platform.medical.dto;

import java.time.Instant;

public record MedicalPriorOrderDTO(
        String procedureCode, String procedureName, String priority,
        String status, Instant requestedAt
) {
}
