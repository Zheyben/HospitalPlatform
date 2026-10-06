package com.hospital.platform.medical.service;

public class InvalidPatientClinicalPageException extends RuntimeException {
    public InvalidPatientClinicalPageException() {
        super("Clinical page limit or offset is invalid");
    }
}
