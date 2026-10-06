package com.hospital.platform.agenda.service;

import com.hospital.platform.agenda.dto.AgendaResponseDTO;
import com.hospital.platform.agenda.dto.AgendaPublicationResponseDTO;
import com.hospital.platform.agenda.dto.AvailabilitySlotResponseDTO;
import com.hospital.platform.agenda.dto.CreateAgendaRequestDTO;
import com.hospital.platform.agenda.dto.UpdateAgendaRequestDTO;
import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import com.hospital.platform.agenda.entity.Schedule;
import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.agenda.exception.AgendaNotFoundException;
import com.hospital.platform.agenda.exception.InactiveAgendaException;
import com.hospital.platform.agenda.exception.AvailabilitySlotNotFoundException;
import com.hospital.platform.agenda.exception.InvalidScheduleTimeException;
import com.hospital.platform.agenda.exception.ProfessionalNotAvailableException;
import com.hospital.platform.agenda.mapper.AgendaMapper;
import com.hospital.platform.agenda.repository.AvailabilitySlotRepository;
import com.hospital.platform.agenda.repository.ScheduleRepository;
import com.hospital.platform.agenda.repository.PatientAvailabilityRow;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {

    private final ScheduleRepository scheduleRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ProfessionalLookupService professionalLookupService;
    private final AgendaMapper agendaMapper;
    private final Clock clock;
    private final CapacityGateway capacityGateway;
    private final SlotGenerationService slotGenerationService;

    public AgendaService(
            ScheduleRepository scheduleRepository,
            AvailabilitySlotRepository availabilitySlotRepository,
            ProfessionalLookupService professionalLookupService,
            Clock clock,
            CapacityGateway capacityGateway,
            SlotGenerationService slotGenerationService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.professionalLookupService = professionalLookupService;
        this.agendaMapper = new AgendaMapper();
        this.clock = clock;
        this.capacityGateway = capacityGateway;
        this.slotGenerationService = slotGenerationService;
    }

    @Transactional
    public AgendaResponseDTO createAgenda(CreateAgendaRequestDTO request) {
        assertActiveProfessional(request.professionalId());
        assertValidTimeRange(request.startTime(), request.endTime());
        assertOperationalAssociation(request.professionalId(), request.specialtyId());
        UUID id = capacityGateway.createSchedule(request.professionalId(), request.specialtyId(),
                request.dayOfWeek(), request.startTime(), request.endTime());
        return agendaMapper.toAgendaResponse(findSchedule(id));
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
        assertOperationalAssociation(request.professionalId(), request.specialtyId());
        capacityGateway.reconfigureSchedule(agendaId, request.professionalId(), request.specialtyId(),
                request.dayOfWeek(), request.startTime(), request.endTime());
        return agendaMapper.toAgendaResponse(findSchedule(agendaId));
    }

    @Transactional
    public AgendaResponseDTO changeAgendaStatus(UUID agendaId, boolean active) {
        findSchedule(agendaId);
        capacityGateway.scheduleStatus(agendaId, active);
        return agendaMapper.toAgendaResponse(findSchedule(agendaId));
    }

    @Transactional
    public AgendaPublicationResponseDTO publishAgenda(UUID agendaId) {
        Schedule schedule = findSchedule(agendaId);
        if (!schedule.isActive()) {
            throw new InactiveAgendaException();
        }
        int created = slotGenerationService.generate(agendaId);
        return new AgendaPublicationResponseDTO(agendaId, created, SlotGenerationService.HORIZON_DAYS);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponseDTO> findAvailability(
            UUID scheduleId,
            UUID professionalId,
            LocalDate slotDate,
            AvailabilitySlotStatus status
    ) {
        if (isPatient()) {
            LocalDateTime now = LocalDateTime.now(clock);
            return availabilitySlotRepository.findPatientAvailability(
                            scheduleId, professionalId, slotDate, now.toLocalDate(), now.toLocalTime())
                    .stream().map(this::toPatientResponse).toList();
        }
        return agendaMapper.toAvailabilityResponseList(
                availabilitySlotRepository.findAvailability(scheduleId, professionalId, slotDate, status)
        );
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponseDTO> findReceptionAvailability(
            UUID scheduleId, UUID professionalId, LocalDate slotDate
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        return availabilitySlotRepository.findPatientAvailability(
                        scheduleId, professionalId, slotDate, now.toLocalDate(), now.toLocalTime())
                .stream().map(this::toPatientResponse).toList();
    }

    private AvailabilitySlotResponseDTO toPatientResponse(PatientAvailabilityRow row) {
        return new AvailabilitySlotResponseDTO(
                row.getId(), row.getScheduleId(), row.getSlotDate(), row.getStartTime(), row.getEndTime(),
                AvailabilitySlotStatus.AVAILABLE, true, row.getProfessionalId(), row.getProfessionalName(),
                row.getSpecialtyId(), row.getSpecialtyName()
        );
    }

    private boolean isPatient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_PATIENT".equals(authority.getAuthority()));
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

    private void assertOperationalAssociation(UUID professionalId, UUID specialtyId) {
        if (!capacityGateway.isOperationalAssociation(professionalId, specialtyId)) {
            throw new IllegalArgumentException("Professional and specialty must have an active association");
        }
    }

    private void assertValidTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidScheduleTimeException();
        }
    }
}
