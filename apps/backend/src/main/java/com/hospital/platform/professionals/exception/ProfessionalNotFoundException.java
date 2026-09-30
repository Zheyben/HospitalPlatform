package com.hospital.platform.professionals.exception;

import java.util.UUID;

public class ProfessionalNotFoundException extends RuntimeException {

    public ProfessionalNotFoundException(UUID professionalId) {
        super("Professional not found: " + professionalId);
    }
}
