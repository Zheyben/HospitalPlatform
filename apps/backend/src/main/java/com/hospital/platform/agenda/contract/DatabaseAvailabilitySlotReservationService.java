package com.hospital.platform.agenda.contract;

import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class DatabaseAvailabilitySlotReservationService implements AvailabilitySlotReservationService {

    private final AvailabilitySlotRepository availabilitySlotRepository;

    DatabaseAvailabilitySlotReservationService(AvailabilitySlotRepository availabilitySlotRepository) {
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public AvailabilitySlotReference reserveUsableSlot(UUID slotId) {
        if (availabilitySlotRepository.reserveUsableSlot(slotId) != 1) {
            throw new SlotReservationRejectedException(slotId);
        }

        AvailabilitySlot slot = availabilitySlotRepository.findByIdWithSchedule(slotId)
                .orElseThrow(() -> new SlotReservationRejectedException(slotId));

        return new AvailabilitySlotReference(
                slot.getId(),
                slot.getSchedule().getProfessionalId(),
                slot.getSchedule().getSpecialtyId(),
                slot.getSlotDate(),
                slot.getStartTime(),
                slot.getEndTime()
        );
    }
}
