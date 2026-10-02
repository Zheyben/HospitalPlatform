package com.hospital.platform.appointments.service;

import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.dto.CreateAppointmentRequestDTO;
import com.hospital.platform.appointments.entity.Appointment;
import com.hospital.platform.appointments.entity.AppointmentStatus;
import com.hospital.platform.appointments.entity.FlowStage;
import com.hospital.platform.appointments.exception.AppointmentNotFoundException;
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
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;
    private final AppointmentMapper appointmentMapper;
    private final CapacityGateway capacityGateway;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientLookupService patientLookupService,
            ProfessionalLookupService professionalLookupService,
            @Lazy AuditLogService auditLogService,
            CurrentUserService currentUserService,
            AppointmentMapper appointmentMapper,
            CapacityGateway capacityGateway
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientLookupService = patientLookupService;
        this.professionalLookupService = professionalLookupService;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
        this.appointmentMapper = appointmentMapper;
        this.capacityGateway = capacityGateway;
    }

    @Transactional
    public AppointmentResponseDTO createAppointment(CreateAppointmentRequestDTO request) {
        UUID patientId = resolvePatientId(request.patientId());
        try {
            UUID appointmentId = capacityGateway.reserve(request.slotId(), patientId, normalizeNullable(request.reason()));
            return appointmentMapper.toResponse(findAppointment(appointmentId));
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
        capacityGateway.lockAppointment(appointmentId);
        Appointment appointment = findAppointment(appointmentId);
        authorizeLifecycleOperation(appointment);
        requireFuture(appointment, "confirmed");

        if (appointment.getAppointmentStatus() == AppointmentStatus.CONFIRMED) {
            return appointmentMapper.toResponse(appointment);
        }
        requireStatus(appointment, "confirmed", AppointmentStatus.SCHEDULED);

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        capacityGateway.confirm(appointmentId);
        appointment = findAppointment(appointmentId);
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
        capacityGateway.lockAppointment(appointmentId);
        Appointment appointment = findAppointment(appointmentId);
        authorizeLifecycleOperation(appointment);
        requireFuture(appointment, "cancelled");

        if (appointment.getAppointmentStatus() == AppointmentStatus.CANCELLED) {
            return appointmentMapper.toResponse(appointment);
        }
        requireStatus(
                appointment,
                "cancelled",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );
        requireNoActiveFlow(appointment, "cancelled");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        UUID actorUserId = currentUserService.currentUserId();
        capacityGateway.cancel(appointmentId, actorUserId);
        appointment = findAppointment(appointmentId);
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
        capacityGateway.lockReschedule(appointmentId, newSlotId);
        Appointment original = findAppointment(appointmentId);
        authorizeLifecycleOperation(original);
        requireFuture(original, "rescheduled");
        requireStatus(
                original,
                "rescheduled",
                AppointmentStatus.SCHEDULED,
                AppointmentStatus.CONFIRMED
        );
        requireNoActiveFlow(original, "rescheduled");
        if (appointmentRepository.findByRescheduledFromId(original.getId()).isPresent()) {
            throw new AppointmentSuccessorExistsException(original.getId());
        }

        UUID successorId;
        try {
            successorId = capacityGateway.reschedule(appointmentId, newSlotId);
        } catch (DataIntegrityViolationException exception) {
            throw new SlotUnavailableException(newSlotId, exception);
        }
        Appointment successor = findAppointment(successorId);
        AppointmentStatus previousStatus = original.getAppointmentStatus();
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
        capacityGateway.lockAppointment(appointmentId);
        Appointment appointment = findAppointment(appointmentId);
        authorizeReceptionOperation();

        if (hasState(appointment, AppointmentStatus.CONFIRMED, FlowStage.CHECK_IN)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, null, "checked in");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        capacityGateway.stage(appointmentId, FlowStage.CHECK_IN.name());
        appointment = findAppointment(appointmentId);
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
        capacityGateway.lockAppointment(appointmentId);
        Appointment appointment = findAppointment(appointmentId);
        authorizeReceptionOperation();

        if (hasState(appointment, AppointmentStatus.CONFIRMED, FlowStage.WAITING)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, FlowStage.CHECK_IN, "moved to waiting");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        capacityGateway.stage(appointmentId, FlowStage.WAITING.name());
        appointment = findAppointment(appointmentId);
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
        capacityGateway.lockAppointment(appointmentId);
        Appointment appointment = findAppointment(appointmentId);
        authorizeProfessionalOperation(appointment);

        if (hasState(appointment, AppointmentStatus.CONFIRMED, FlowStage.IN_ATTENTION)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, FlowStage.WAITING, "started attention");

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        capacityGateway.stage(appointmentId, FlowStage.IN_ATTENTION.name());
        appointment = findAppointment(appointmentId);
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
        capacityGateway.lockAppointment(appointmentId);
        Appointment appointment = findAppointment(appointmentId);
        authorizeProfessionalOperation(appointment);

        if (hasState(appointment, AppointmentStatus.COMPLETED, FlowStage.FINISHED)) {
            return appointmentMapper.toResponse(appointment);
        }
        requireFlowState(appointment, FlowStage.IN_ATTENTION, "completed");
        if (capacityGateway.isFutureSlot(appointment.getSlotId())) {
            throw new InvalidAppointmentTransitionException(
                    appointment.getId(), appointment.getAppointmentStatus(), "completed before slot start");
        }

        AppointmentStatus previousStatus = appointment.getAppointmentStatus();
        FlowStage previousStage = appointment.getFlowStage();
        capacityGateway.complete(appointmentId);
        appointment = findAppointment(appointmentId);
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

    private Appointment findAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
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

    private void requireFuture(Appointment appointment, String operation) {
        if (!capacityGateway.isFutureSlot(appointment.getSlotId())) {
            throw new InvalidAppointmentTransitionException(
                    appointment.getId(), appointment.getAppointmentStatus(), operation);
        }
    }

    private void requireNoActiveFlow(Appointment appointment, String operation) {
        if (appointment.getFlowStage() == FlowStage.CHECK_IN
                || appointment.getFlowStage() == FlowStage.WAITING
                || appointment.getFlowStage() == FlowStage.IN_ATTENTION) {
            throw new InvalidAppointmentTransitionException(
                    appointment.getId(), appointment.getAppointmentStatus(), operation);
        }
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
