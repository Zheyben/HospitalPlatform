package com.hospital.platform.medical.service;

import com.hospital.platform.appointments.dto.AppointmentResponseDTO;
import com.hospital.platform.appointments.exception.InvalidAppointmentTransitionException;
import com.hospital.platform.appointments.repository.AppointmentRepository;
import com.hospital.platform.appointments.service.AppointmentService;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.medical.dto.MedicalEncounterStartDTO;
import com.hospital.platform.medical.repository.MedicalEncounterRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

@Service
public class MedicalEncounterService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final AppointmentService appointments;
    private final AppointmentRepository appointmentRepository;
    private final MedicalEncounterRepository encounters;
    private final AuditLogService audit;
    private final Clock clock;

    public MedicalEncounterService(AppointmentService appointments, AppointmentRepository appointmentRepository,
                                   MedicalEncounterRepository encounters, @Lazy AuditLogService audit, Clock clock) {
        this.appointments = appointments;
        this.appointmentRepository = appointmentRepository;
        this.encounters = encounters;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public StartResult start(UUID appointmentId) {
        AppointmentResponseDTO appointment = appointments.startAppointmentAttention(appointmentId);
        Optional<MedicalEncounterStartDTO> existing = encounters.findByAppointmentId(appointmentId);
        if (existing.isPresent()) {
            return new StartResult(existing.get(), appointment);
        }
        if (!LocalDate.now(clock.withZone(LIMA))
                .equals(appointmentRepository.findSlotDateByAppointmentId(appointmentId))) {
            throw new InvalidAppointmentTransitionException(
                    appointmentId, appointment.appointmentStatus(), "started outside appointment date");
        }

        UUID encounterId = UUID.randomUUID();
        int inserted = encounters.insertOpen(encounterId, appointmentId, clock.instant());
        MedicalEncounterStartDTO encounter = encounters.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new IllegalStateException("Attention has no clinical encounter"));
        if (inserted == 1) {
            audit.record(AuditEventType.CLINICAL_ENCOUNTER_STARTED, "ClinicalEncounter", encounter.encounterId(),
                    Map.of(), Map.of("appointmentId", appointmentId.toString()));
        }
        return new StartResult(encounter, appointment);
    }

    public record StartResult(MedicalEncounterStartDTO encounter, AppointmentResponseDTO appointment) {
    }
}
