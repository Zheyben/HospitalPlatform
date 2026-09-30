package com.hospital.platform.agenda.contract;

import java.util.UUID;

public interface AvailabilitySlotService {

    boolean existsSlot(UUID slotId);

    boolean isAvailable(UUID slotId);

    boolean isUsable(UUID slotId);
}
