package com.hospital.platform.medical.service;

import com.hospital.platform.medical.dto.MedicalAppointmentContextDTO;
import com.hospital.platform.medical.dto.MedicalPriorEncounterDTO;
import com.hospital.platform.medical.dto.MedicalProfessionalContextDTO;
import com.hospital.platform.medical.dto.PatientClinicalPageDTO;
import com.hospital.platform.medical.repository.MedicalContextRepository;
import com.hospital.platform.medical.repository.MedicalContextRepository.ProfessionalRow;
import com.hospital.platform.users.service.CurrentUserService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalContextService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final int MAX_HISTORY_LIMIT = 50;
    private static final int MAX_HISTORY_OFFSET = 10_000;

    private final MedicalContextRepository repository;
    private final CurrentUserService currentUser;
    private final Clock clock;

    public MedicalContextService(MedicalContextRepository repository, CurrentUserService currentUser, Clock clock) {
        this.repository = repository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MedicalProfessionalContextDTO ownContext() {
        ProfessionalRow professional = currentProfessional();
        return new MedicalProfessionalContextDTO(
                professional.id(), professional.firstName(), professional.lastName(),
                professional.licenseNumber(), professional.simulatedRne(),
                professional.specialtyId(), professional.specialtyName(),
                repository.findReadyAppointments(professional.id(), LocalDate.now(clock.withZone(LIMA))));
    }

    @Transactional(readOnly = true)
    public MedicalAppointmentContextDTO appointmentContext(UUID appointmentId) {
        ProfessionalRow professional = currentProfessional();
        return repository.findAssignedAppointment(professional.id(), appointmentId,
                        LocalDate.now(clock.withZone(LIMA)))
                .orElseThrow(MedicalAppointmentNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public PatientClinicalPageDTO<MedicalPriorEncounterDTO> priorEncounters(
            UUID appointmentId, int limit, int offset
    ) {
        ProfessionalRow professional = currentProfessional();
        if (limit < 1 || limit > MAX_HISTORY_LIMIT || offset < 0 || offset > MAX_HISTORY_OFFSET) {
            throw new InvalidMedicalHistoryPageException();
        }
        LocalDate today = LocalDate.now(clock.withZone(LIMA));
        repository.findAssignedAppointment(professional.id(), appointmentId, today)
                .orElseThrow(MedicalAppointmentNotFoundException::new);
        List<MedicalPriorEncounterDTO> rows = repository.findPriorEncounters(
                appointmentId, professional.id(), today, limit + 1, offset);
        return new PatientClinicalPageDTO<>(
                List.copyOf(rows.subList(0, Math.min(limit, rows.size()))),
                limit, offset, rows.size() > limit);
    }

    public UUID requireCurrentProfessionalId() {
        return currentProfessional().id();
    }

    private ProfessionalRow currentProfessional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_PROFESSIONAL".equals(authority.getAuthority()))) {
            throw new AccessDeniedException("Professional role required");
        }
        return repository.findActiveProfessional(currentUser.currentUserId())
                .orElseThrow(() -> new AccessDeniedException("Active professional required"));
    }
}
