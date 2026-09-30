package com.hospital.platform.agenda.service;

import com.hospital.platform.agenda.dto.AgendaResponseDTO;
import com.hospital.platform.agenda.dto.AvailabilitySlotResponseDTO;
import com.hospital.platform.agenda.dto.CreateAgendaRequestDTO;
import com.hospital.platform.agenda.dto.UpdateAgendaRequestDTO;
import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import com.hospital.platform.agenda.entity.Schedule;
import com.hospital.platform.agenda.exception.AgendaNotFoundException;
import com.hospital.platform.agenda.exception.AvailabilitySlotNotFoundException;
import com.hospital.platform.agenda.exception.InvalidScheduleTimeException;
import com.hospital.platform.agenda.exception.ProfessionalNotAvailableException;
import com.hospital.platform.agenda.mapper.AgendaMapper;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import com.hospital.platform.agenda.repository.ScheduleRepository;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {

    private final ScheduleRepository scheduleRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ProfessionalLookupService professionalLookupService;
    private final AgendaMapper agendaMapper;

    public AgendaService(
            ScheduleRepository scheduleRepository,
            AvailabilitySlotRepository availabilitySlotRepository,
            ProfessionalLookupService professionalLookupService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.professionalLookupService = professionalLookupService;
        this.agendaMapper = new AgendaMapper();
    }

    @Transactional
    public AgendaResponseDTO createAgenda(CreateAgendaRequestDTO request) {
        assertActiveProfessional(request.professionalId());
        assertValidTimeRange(request.startTime(), request.endTime());

        Schedule schedule = new Schedule(
                null,
                request.professionalId(),
                request.specialtyId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        return agendaMapper.toAgendaResponse(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<AgendaResponseDTO> findAgendas(UUID professionalId, UUID specialtyId) {
        return agendaMapper.toAgendaResponseList(
                scheduleRepository.findSchedules(professionalId, specialtyId)
        );
    }

    @Transactional(readOnly = true)
    public AgendaResponseDTO findAgendaById(UUID agendaId) {
        return agendaMapper.toAgendaResponse(findSchedule(agendaId));
    }

    @Transactional
    public AgendaResponseDTO updateAgenda(UUID agendaId, UpdateAgendaRequestDTO request) {
        Schedule schedule = findSchedule(agendaId);
        assertActiveProfessional(request.professionalId());
        assertValidTimeRange(request.startTime(), request.endTime());

        schedule.updateConfiguration(
                request.professionalId(),
                request.specialtyId(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );

        return agendaMapper.toAgendaResponse(schedule);
    }

    @Transactional
    public AgendaResponseDTO changeAgendaStatus(UUID agendaId, boolean active) {
        Schedule schedule = findSchedule(agendaId);
        schedule.changeStatus(active);
        return agendaMapper.toAgendaResponse(schedule);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponseDTO> findAvailability(
            UUID scheduleId,
            UUID professionalId,
            LocalDate slotDate,
            AvailabilitySlotStatus status
    ) {
        return agendaMapper.toAvailabilityResponseList(
                availabilitySlotRepository.findAvailability(scheduleId, professionalId, slotDate, status)
        );
    }

    @Transactional(readOnly = true)
    public AvailabilitySlotResponseDTO findAvailabilitySlotById(UUID slotId) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new AvailabilitySlotNotFoundException(slotId));
        return agendaMapper.toAvailabilityResponse(slot);
    }

    private Schedule findSchedule(UUID agendaId) {
        return scheduleRepository.findById(agendaId)
                .orElseThrow(() -> new AgendaNotFoundException(agendaId));
    }

    private void assertActiveProfessional(UUID professionalId) {
        if (!professionalLookupService.existsActiveProfessional(professionalId)) {
            throw new ProfessionalNotAvailableException(professionalId);
        }
    }

    private void assertValidTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidScheduleTimeException();
        }
    }
}
