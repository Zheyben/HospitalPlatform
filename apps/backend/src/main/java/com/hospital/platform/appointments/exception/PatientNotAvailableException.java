package com.hospital.platform.appointments.exception;

import java.util.UUID;

public class PatientNotAvailableException extends RuntimeException {

    public PatientNotAvailableException(UUID patientId) {
        super("Active patient not found: " + patientId);
    }

    public PatientNotAvailableException(String message) {
        super(message);
    }
}
