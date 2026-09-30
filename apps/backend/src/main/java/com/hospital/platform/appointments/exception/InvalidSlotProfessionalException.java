package com.hospital.platform.appointments.exception;

public class InvalidSlotProfessionalException extends RuntimeException {

    public InvalidSlotProfessionalException() {
        super("The professional does not correspond to the availability slot");
    }
}
