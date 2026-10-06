package com.hospital.platform.patients.domain;

public class InvalidPatientDemographicsException extends RuntimeException {
    public InvalidPatientDemographicsException(String message) {
        super(message);
    }
}
