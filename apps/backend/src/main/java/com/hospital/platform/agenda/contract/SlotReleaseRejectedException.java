package com.hospital.platform.agenda.contract;

import java.util.UUID;

public class SlotReleaseRejectedException extends RuntimeException {

    public SlotReleaseRejectedException(UUID slotId) {
        super("Availability slot cannot be released: " + slotId);
    }
}
