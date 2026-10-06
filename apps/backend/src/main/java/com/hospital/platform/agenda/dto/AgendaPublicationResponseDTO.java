package com.hospital.platform.agenda.dto;

import java.util.UUID;

public record AgendaPublicationResponseDTO(UUID scheduleId, int createdSlots, int horizonDays) {
}
