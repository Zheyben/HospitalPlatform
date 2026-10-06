package com.hospital.platform.medical.service;

public class PatientClinicalRecordNotFoundException extends RuntimeException {
    public PatientClinicalRecordNotFoundException() {
        super("Finalized clinical record not found");
    }
}
