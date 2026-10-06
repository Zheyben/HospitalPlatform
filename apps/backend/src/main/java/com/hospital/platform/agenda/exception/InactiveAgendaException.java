package com.hospital.platform.agenda.exception;

public class InactiveAgendaException extends RuntimeException {

    public InactiveAgendaException() {
        super("Inactive schedules cannot publish availability");
    }
}
