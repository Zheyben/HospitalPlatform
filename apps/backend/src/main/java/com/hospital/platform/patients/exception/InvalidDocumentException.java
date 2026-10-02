package com.hospital.platform.patients.exception;

public class InvalidDocumentException extends RuntimeException {
    public InvalidDocumentException() {
        super("Document type or number is invalid");
    }
}
