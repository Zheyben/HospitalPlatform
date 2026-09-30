package com.hospital.platform.agenda.exception;

public class InvalidScheduleTimeException extends RuntimeException {

    public InvalidScheduleTimeException() {
        super("Agenda end time must be after start time");
    }
}
