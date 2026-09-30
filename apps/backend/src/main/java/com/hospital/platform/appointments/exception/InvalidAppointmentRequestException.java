package com.hospital.platform.appointments.exception;

public class InvalidAppointmentRequestException extends RuntimeException {

    public InvalidAppointmentRequestException(String message) {
        super(message);
    }
}
