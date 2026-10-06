package com.hospital.platform.catalogs.dto;

import java.util.UUID;

public record MedicationOptionDTO(UUID id, String genericName, String commercialName) {
}
