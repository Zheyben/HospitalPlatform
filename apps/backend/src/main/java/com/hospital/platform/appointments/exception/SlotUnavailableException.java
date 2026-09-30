package com.hospital.platform.appointments.exception;

import java.util.UUID;

public class SlotUnavailableException extends RuntimeException {

    public SlotUnavailableException(UUID slotId) {
        super("Availability slot cannot be reserved: " + slotId);
    }

    public SlotUnavailableException(UUID slotId, Throwable cause) {
        super("Availability slot cannot be reserved: " + slotId, cause);
    }
}
