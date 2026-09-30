package com.hospital.platform.appointments.exception;

import java.util.UUID;

public class AppointmentSuccessorExistsException extends RuntimeException {

    public AppointmentSuccessorExistsException(UUID appointmentId) {
        super("Appointment already has a rescheduled successor: " + appointmentId);
    }
}
