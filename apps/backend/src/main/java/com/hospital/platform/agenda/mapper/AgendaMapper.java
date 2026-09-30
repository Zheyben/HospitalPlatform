package com.hospital.platform.agenda.mapper;

import com.hospital.platform.agenda.dto.AgendaResponseDTO;
import com.hospital.platform.agenda.dto.AvailabilitySlotResponseDTO;
import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.Schedule;
import java.util.List;

public class AgendaMapper {

    public AgendaResponseDTO toAgendaResponse(Schedule schedule) {
        return new AgendaResponseDTO(
                schedule.getId(),
                schedule.getProfessionalId(),
                schedule.getSpecialtyId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.isActive()
        );
    }

    public List<AgendaResponseDTO> toAgendaResponseList(List<Schedule> schedules) {
        return schedules.stream()
                .map(this::toAgendaResponse)
                .toList();
    }

    public AvailabilitySlotResponseDTO toAvailabilityResponse(AvailabilitySlot slot) {
        return new AvailabilitySlotResponseDTO(
                slot.getId(),
                slot.getSchedule().getId(),
                slot.getSlotDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus(),
                slot.isUsable()
        );
    }

    public List<AvailabilitySlotResponseDTO> toAvailabilityResponseList(List<AvailabilitySlot> slots) {
        return slots.stream()
                .map(this::toAvailabilityResponse)
                .toList();
    }
}
