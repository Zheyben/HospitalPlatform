package com.hospital.platform.patients.exception;

public class DuplicateDocumentException extends RuntimeException {

    public DuplicateDocumentException(String documentNumber) {
        super("Patient document already exists: " + documentNumber);
    }
}
