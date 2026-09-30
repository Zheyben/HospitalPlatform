package com.hospital.platform.appointments.exception;

import java.util.UUID;

public class ProfessionalNotAvailableException extends RuntimeException {

    public ProfessionalNotAvailableException(UUID professionalId) {
        super("Active professional not found: " + professionalId);
    }
}
