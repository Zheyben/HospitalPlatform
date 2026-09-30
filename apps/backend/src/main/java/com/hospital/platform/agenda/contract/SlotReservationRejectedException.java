package com.hospital.platform.agenda.contract;

import java.util.UUID;

public class SlotReservationRejectedException extends RuntimeException {

    public SlotReservationRejectedException(UUID slotId) {
        super("Availability slot cannot be reserved: " + slotId);
    }
}
