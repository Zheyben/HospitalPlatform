package com.hospital.platform.agenda.contract;

import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DatabaseAvailabilitySlotService implements AvailabilitySlotService {

    private final AvailabilitySlotRepository availabilitySlotRepository;

    DatabaseAvailabilitySlotService(AvailabilitySlotRepository availabilitySlotRepository) {
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsSlot(UUID slotId) {
        return availabilitySlotRepository.existsById(slotId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(UUID slotId) {
        return availabilitySlotRepository.existsByIdAndStatus(slotId, AvailabilitySlotStatus.AVAILABLE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsable(UUID slotId) {
        return availabilitySlotRepository.findByIdWithSchedule(slotId)
                .map(AvailabilitySlot::isUsable)
                .orElse(false);
    }
}
