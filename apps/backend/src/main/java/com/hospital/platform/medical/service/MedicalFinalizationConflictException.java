package com.hospital.platform.medical.service;

public class MedicalFinalizationConflictException extends RuntimeException {
    public MedicalFinalizationConflictException() {
        super("Clinical encounter cannot be finalized with this version or state");
    }
}
