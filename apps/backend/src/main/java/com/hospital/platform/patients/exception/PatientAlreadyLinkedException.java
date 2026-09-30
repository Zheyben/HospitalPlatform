package com.hospital.platform.patients.exception;

import java.util.UUID;

public class PatientAlreadyLinkedException extends RuntimeException {

    public PatientAlreadyLinkedException(UUID patientId) {
        super("Patient is already linked to a user: " + patientId);
    }
}
