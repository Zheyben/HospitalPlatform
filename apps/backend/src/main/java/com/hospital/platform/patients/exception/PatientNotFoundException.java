package com.hospital.platform.patients.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(UUID patientId) {
        super("Patient not found: " + patientId);
    }

    public PatientNotFoundException(String message) {
        super(message);
    }
}
