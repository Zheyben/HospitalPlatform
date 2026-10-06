package com.hospital.platform.catalogs.dto;

import java.util.UUID;

public record MedicationPresentationDTO(
        UUID id,
        UUID medicationId,
        String name,
        String concentration,
        String pharmaceuticalForm
) {
}
