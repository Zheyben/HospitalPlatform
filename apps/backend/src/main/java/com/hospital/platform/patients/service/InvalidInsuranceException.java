package com.hospital.platform.patients.service;

public class InvalidInsuranceException extends RuntimeException {
    public InvalidInsuranceException() {
        super("Invalid insurance selection");
    }
}
