package com.hospital.platform.medical.service;

public class InvalidMedicalFinalizationException extends RuntimeException {
    public InvalidMedicalFinalizationException() {
        super("Clinical finalization data is incomplete or invalid");
    }
}
