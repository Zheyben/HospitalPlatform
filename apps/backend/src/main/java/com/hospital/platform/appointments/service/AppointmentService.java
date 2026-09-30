package com.hospital.platform.appointments.service;

import com.hospital.platform.agenda.contract.AvailabilitySlotReference;
import com.hospital.platform.agenda.contract.AvailabilitySlotReleaseService;
import com.hospital.platform.agenda.contract.AvailabilitySlotReservationService;
import com.hospital.platform.agenda.contract.SlotReleaseRejectedException;
import com.hospital.platform.agenda.contract.SlotReservationRejectedException;
import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import com.hospital.platform.appointments.exception.AppointmentNotFoundException;
import com.hospital.platform.appointments.exception.AppointmentSlotReleaseException;
import com.hospital.platform.appointments.exception.AppointmentSuccessorExistsException;
import com.hospital.platform.appointments.exception.InvalidAppointmentRequestException;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.exception.PatientNotAvailableException;
import com.hospital.platform.appointments.exception.ProfessionalNotAvailableException;
import com.hospital.platform.appointments.exception.SlotUnavailableException;
import com.hospital.platform.appointments.mapper.AppointmentMapper;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.professionals.contract.ProfessionalLookupService;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private static final String ROLE_PATIENT = "ROLE_PATIENT";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_RECEPTIONIST = "ROLE_RECEPTIONIST";
    private static final String ROLE_PROFESSIONAL = "ROLE_PROFESSIONAL";

    private final AppointmentRepository appointmentRepository;
    private final PatientLookupService patientLookupService;
    private final ProfessionalLookupService professionalLookupService;
    private final AvailabilitySlotReservationService slotReservationService;
    private final AvailabilitySlotReleaseService slotReleaseService;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientLookupService patientLookupService,
            ProfessionalLookupService professionalLookupService,
            AvailabilitySlotReservationService slotReservationService,
            AvailabilitySlotReleaseService slotReleaseService,
            @Lazy AuditLogService auditLogService,
            CurrentUserService currentUserService,
            AppointmentMapper appointmentMapper
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientLookupService = patientLookupService;
        this.professionalLookupService = professionalLookupService;
        this.slotReservationService = slotReservationService;
        this.slotReleaseService = slotReleaseService;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
        this.appointmentMapper = appointmentMapper;
    }

    @Transactional
    public AppointmentResponseDTO createAppointment(CreateAppointmentRequestDTO request) {
        UUID patientId = resolvePatientId(request.patientId());
        AvailabilitySlotReference slot = reserveSlot(request.slotId());

        if (!professionalLookupService.existsActiveProfessional(slot.professionalId())) {
            throw new ProfessionalNotAvailableException(slot.professionalId());
        }

        Appointment appointment = new Appointment(
                null,
                patientId,
                slot.professionalId(),
                slot.slotId(),
                normalizeNullable(request.reason())
        );

        try {
            return appointmentMapper.toResponse(appointmentRepository.saveAndFlush(appointment));
        } catch (DataIntegrityViolationException exception) {
            throw new SlotUnavailableException(request.slotId(), exception);
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> findAppointments() {
        if (hasAdministrativeRole()) {
            return appointmentMapper.toResponseList(appointmentRepository.findAllByOrderByCreatedAtDesc());
        }
        if (hasRole(ROLE_PATIENT)) {
            UUID patientId = findCurrentPatientId();
            return appointmentMapper.toResponseList(
                    appointmentRepository.findAllByPatientIdOrderByCreatedAtDesc(patientId)
            );
        }
        throw new AccessDeniedException("Appointment access denied");
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO findAppointmentById(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!hasAdministrativeRole()) {
            if (!hasRole(ROLE_PATIENT) || !appointment.getPatientId().equals(findCurrentPatientId())) {
                throw new AccessDeniedException("Appointment access denied");
            }
        }

        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponseDTO confirmAppointment(UUID appointmentId) {
        Appointment appointment = findAppointmentForUpdate(appointmentId);
        authorizeLifecycleOperation(appointment);

        if (appointment.getAppointmentStatus() == AppointmentStatus.CONFIRMED) {
            return appointmentMapper.toResponse(appointment);
        }
        requireStatus(appointment, "confirmed", AppointmentStatus.SCHEDULED);

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        appointment.confirm();
        recordStatusTransition(
                AuditEventType.APPOINTMENT_CONFIRMED,
                appointment,
                previousStatus,
                AppointmentStatus.CONFIRMED
        );
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponseDTO cancelAppointment(UUID appointmentId) {
        Appointment appointment = findAppointmentForUpdate(appointmentId);
        authorizeLifecycleOperation(appointment);

        if (appointment.getAppointmentStatus() == AppointmentStatus.CANCELLED) {
            return appointmentMapper.toResponse(appointment);
        }
        requireStatus(
                appointment,
                "cancelled",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        UUID actorUserId = currentUserService.currentUserId();
        appointment.cancel(LocalDateTime.now(), actorUserId);
        releaseSlot(appointment.getSlotId());
        recordStatusTransition(
                AuditEventType.APPOINTMENT_CANCELLED,
                appointment,
                previousStatus,
                AppointmentStatus.CANCELLED
        );
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(UUID appointmentId, UUID newSlotId) {
        Appointment original = findAppointmentForUpdate(appointmentId);
        authorizeLifecycleOperation(original);
        requireStatus(
                original,
                "rescheduled",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );
        if (appointmentRepository.findByRescheduledFromId(original.getId()).isPresent()) {
            throw new AppointmentSuccessorExistsException(original.getId());
        }

        AvailabilitySlotReference newSlot = reserveSlot(newSlotId);
        if (!professionalLookupService.existsActiveProfessional(newSlot.professionalId())) {
            throw new ProfessionalNotAvailableException(newSlot.professionalId());
        }

        Appointment successor = new Appointment(
                UUID.randomUUID(),
                original.getPatientId(),
                newSlot.professionalId(),
                newSlot.slotId(),
                original.getReason(),
                original.getId()
        );

        try {
            appointmentRepository.saveAndFlush(successor);
        } catch (DataIntegrityViolationException exception) {
            throw new SlotUnavailableException(newSlotId, exception);
        }

        AppointmentStatus previousStatus = original.getAppointmentStatus();
        original.markRescheduled();
        appointmentRepository.saveAndFlush(original);
        releaseSlot(original.getSlotId());
        auditLogService.record(
                AuditEventType.APPOINTMENT_RESCHEDULED,
                "Appointment",
                original.getId(),
                Map.of(
                        "appointmentStatus", previousStatus.name(),
                        "slotId", original.getSlotId().toString()
                ),
                Map.of(
                        "appointmentStatus", AppointmentStatus.RESCHEDULED.name(),
                        "successorAppointmentId", successor.getId().toString(),
                        "slotId", successor.getSlotId().toString()
                )
        );
        return appointmentMapper.toResponse(successor);
    }

    @Transactional
    public AppointmentResponseDTO checkInAppointment(UUID appointmentId) {
        Appointment appointment = findAppointmentForUpdate(appointmentId);
        authorizeReceptionOperation();

        if (hasState(appointment, AppointmentStatus.CONFIRMED, FlowStage.CHECK_IN)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, null, "checked in");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        appointment.checkIn();
        recordFlowTransition(
                AuditEventType.APPOINTMENT_CHECKED_IN,
                appointment,
                previousStatus,
                previousStage
        );
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponseDTO moveAppointmentToWaiting(UUID appointmentId) {
        Appointment appointment = findAppointmentForUpdate(appointmentId);
        authorizeReceptionOperation();

        if (hasState(appointment, AppointmentStatus.CONFIRMED, FlowStage.WAITING)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, FlowStage.CHECK_IN, "moved to waiting");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        appointment.moveToWaiting();
        recordFlowTransition(
                AuditEventType.APPOINTMENT_WAITING,
                appointment,
                previousStatus,
                previousStage
        );
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponseDTO startAppointmentAttention(UUID appointmentId) {
        Appointment appointment = findAppointmentForUpdate(appointmentId);
        authorizeProfessionalOperation(appointment);

        if (hasState(appointment, AppointmentStatus.CONFIRMED, FlowStage.IN_ATTENTION)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, FlowStage.WAITING, "started attention");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        appointment.startAttention();
        recordFlowTransition(
                AuditEventType.APPOINTMENT_ATTENTION_STARTED,
                appointment,
                previousStatus,
                previousStage
        );
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponseDTO completeAppointment(UUID appointmentId) {
        Appointment appointment = findAppointmentForUpdate(appointmentId);
        authorizeProfessionalOperation(appointment);

        if (hasState(appointment, AppointmentStatus.COMPLETED, FlowStage.FINISHED)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, FlowStage.IN_ATTENTION, "completed");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        appointment.complete();
        recordFlowTransition(
                AuditEventType.APPOINTMENT_COMPLETED,
                appointment,
                previousStatus,
                previousStage
        );
        return appointmentMapper.toResponse(appointment);
    }

    private UUID resolvePatientId(UUID requestedPatientId) {
        if (requestedPatientId != null) {
            if (!hasAdministrativeRole()) {
                throw new AccessDeniedException("Patients cannot create appointments for another patient");
            }
            if (!patientLookupService.existsActivePatient(requestedPatientId)) {
                throw new PatientNotAvailableException(requestedPatientId);
            }
            return requestedPatientId;
        }

        if (!hasRole(ROLE_PATIENT)) {
            throw new InvalidAppointmentRequestException("patientId is required for administrative creation");
        }
        return findCurrentPatientId();
    }

    private UUID findCurrentPatientId() {
        UUID userId = currentUserService.currentUserId();
        return patientLookupService.findActivePatientReferenceByUserId(userId)
                .map(reference -> reference.id())
                .orElseThrow(() -> new PatientNotAvailableException(
                        "Active patient profile not found for current user"
                ));
    }

    private AvailabilitySlotReference reserveSlot(UUID slotId) {
        try {
            return slotReservationService.reserveUsableSlot(slotId);
        } catch (SlotReservationRejectedException exception) {
            throw new SlotUnavailableException(slotId, exception);
        }
    }

    private Appointment findAppointmentForUpdate(UUID appointmentId) {
        return appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
    }

    private void authorizeLifecycleOperation(Appointment appointment) {
        if (hasAdministrativeRole()) {
            if (!patientLookupService.existsActivePatient(appointment.getPatientId())) {
                throw new PatientNotAvailableException(appointment.getPatientId());
            }
            return;
        }
        if (!hasRole(ROLE_PATIENT) || !appointment.getPatientId().equals(findCurrentPatientId())) {
            throw new AccessDeniedException("Appointment access denied");
        }
    }

    private void authorizeReceptionOperation() {
        if (!hasRole(ROLE_RECEPTIONIST)) {
            throw new AccessDeniedException("Appointment operation access denied");
        }
    }

    private void authorizeProfessionalOperation(Appointment appointment) {
        if (!hasRole(ROLE_PROFESSIONAL)) {
            throw new AccessDeniedException("Appointment operation access denied");
        }
        UUID userId = currentUserService.currentUserId();
        if (!professionalLookupService.isActiveProfessionalLinkedToUser(
                appointment.getProfessionalId(),
                userId
        )) {
            throw new AccessDeniedException("Appointment professional ownership denied");
        }
    }

    private void requireFlowState(Appointment appointment, FlowStage requiredStage, String operation) {
        if (appointment.getAppointmentStatus() == AppointmentStatus.CONFIRMED
                && appointment.getFlowStage() == requiredStage) {
            return;
        }
        throw new InvalidAppointmentTransitionException(
                appointment.getId(),
                appointment.getAppointmentStatus(),
                operation
        );
    }

    private boolean hasState(
            Appointment appointment,
            AppointmentStatus status,
            FlowStage flowStage
    ) {
        return appointment.getAppointmentStatus() == status
                && appointment.getFlowStage() == flowStage;
    }

    private void requireStatus(
            Appointment appointment,
            String operation,
            AppointmentStatus... allowedStatuses
    ) {
        for (AppointmentStatus allowedStatus : allowedStatuses) {
            if (appointment.getAppointmentStatus() == allowedStatus) {
                return;
            }
        }
        throw new InvalidAppointmentTransitionException(
                appointment.getId(),
                appointment.getAppointmentStatus(),
                operation
        );
    }

    private void releaseSlot(UUID slotId) {
        try {
            slotReleaseService.releaseReservedSlot(slotId);
        } catch (SlotReleaseRejectedException exception) {
            throw new AppointmentSlotReleaseException(slotId, exception);
        }
    }

    private void recordStatusTransition(
            AuditEventType eventType,
            Appointment appointment,
            AppointmentStatus previousStatus,
            AppointmentStatus newStatus
    ) {
        auditLogService.record(
                eventType,
                "Appointment",
                appointment.getId(),
                Map.of("appointmentStatus", previousStatus.name()),
                Map.of("appointmentStatus", newStatus.name())
        );
    }

    private void recordFlowTransition(
            AuditEventType eventType,
            Appointment appointment,
            AppointmentStatus previousStatus,
            FlowStage previousStage
    ) {
        auditLogService.record(
                eventType,
                "Appointment",
                appointment.getId(),
                auditState(previousStatus, previousStage),
                auditState(appointment.getAppointmentStatus(), appointment.getFlowStage())
        );
    }

    private Map<String, Object> auditState(AppointmentStatus status, FlowStage flowStage) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("appointmentStatus", status.name());
        values.put("flowStage", flowStage == null ? null : flowStage.name());
        return values;
    }

    private boolean hasAdministrativeRole() {
        return hasRole(ROLE_ADMIN) || hasRole(ROLE_RECEPTIONIST);
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
