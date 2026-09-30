package com.hospital.platform.agenda.contract;

import java.util.UUID;

public interface AvailabilitySlotReservationService {

    AvailabilitySlotReference reserveUsableSlot(UUID slotId);
}
