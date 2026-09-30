package com.hospital.platform.agenda.exception;

import java.util.UUID;

public class AgendaNotFoundException extends RuntimeException {

    public AgendaNotFoundException(UUID agendaId) {
        super("Agenda not found: " + agendaId);
    }
}
