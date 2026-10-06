package com.hospital.platform.medical.service;

public class InvalidMedicalHistoryPageException extends RuntimeException {
    public InvalidMedicalHistoryPageException() {
        super("Medical history limit or offset is invalid");
    }
}
