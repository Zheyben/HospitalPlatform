package com.hospital.platform.agenda.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateAgendaStatusRequestDTO(
        @NotNull
        Boolean active
) {
}
