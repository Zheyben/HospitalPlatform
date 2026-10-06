package com.hospital.platform.professionals.exception;

public class SpecialtyNotAvailableException extends RuntimeException {
    public SpecialtyNotAvailableException() {
        super("Specialty is not available");
    }
}
