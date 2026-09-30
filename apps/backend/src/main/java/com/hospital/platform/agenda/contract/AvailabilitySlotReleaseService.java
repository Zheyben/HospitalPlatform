package com.hospital.platform.agenda.contract;

import java.util.UUID;

public interface AvailabilitySlotReleaseService {

    void releaseReservedSlot(UUID slotId);
}
