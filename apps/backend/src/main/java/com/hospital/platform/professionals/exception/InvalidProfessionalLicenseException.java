package com.hospital.platform.professionals.exception;

public class InvalidProfessionalLicenseException extends RuntimeException {
    public InvalidProfessionalLicenseException() {
        super("Professional license must contain 4 to 6 digits");
    }
}
