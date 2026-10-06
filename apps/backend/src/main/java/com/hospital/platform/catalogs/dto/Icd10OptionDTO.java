package com.hospital.platform.catalogs.dto;

import java.util.UUID;

public record Icd10OptionDTO(UUID id, String code, String description) {
}
