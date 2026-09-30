package com.hospital.platform.agenda.contract;

import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class DatabaseAvailabilitySlotReleaseService implements AvailabilitySlotReleaseService {

    private final AvailabilitySlotRepository availabilitySlotRepository;

    DatabaseAvailabilitySlotReleaseService(AvailabilitySlotRepository availabilitySlotRepository) {
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void releaseReservedSlot(UUID slotId) {
        if (availabilitySlotRepository.releaseReservedSlot(slotId) != 1) {
            throw new SlotReleaseRejectedException(slotId);
        }
    }
}
