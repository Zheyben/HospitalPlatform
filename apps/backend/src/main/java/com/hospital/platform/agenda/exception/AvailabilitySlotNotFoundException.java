package com.hospital.platform.agenda.exception;

import java.util.UUID;

public class AvailabilitySlotNotFoundException extends RuntimeException {

    public AvailabilitySlotNotFoundException(UUID slotId) {
        super("Availability slot not found: " + slotId);
    }
}
