package com.hospital.platform.appointments.exception;

import java.util.UUID;

public class AppointmentSlotReleaseException extends RuntimeException {

    public AppointmentSlotReleaseException(UUID slotId, Throwable cause) {
        super("Availability slot cannot be released: " + slotId, cause);
    }
}
