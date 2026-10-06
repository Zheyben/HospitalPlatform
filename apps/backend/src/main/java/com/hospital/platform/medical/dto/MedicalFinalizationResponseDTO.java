package com.hospital.platform.medical.dto;

import java.time.Instant;
import java.util.UUID;

public record MedicalFinalizationResponseDTO(UUID encounterId, UUID appointmentId, String status,
                                             Instant finalizedAt, String prescriptionNumber) {
}
