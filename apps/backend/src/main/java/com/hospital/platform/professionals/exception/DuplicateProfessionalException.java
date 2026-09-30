package com.hospital.platform.professionals.exception;

public class DuplicateProfessionalException extends RuntimeException {

    public DuplicateProfessionalException(String licenseNumber) {
        super("Professional license number already exists: " + licenseNumber);
    }
}
