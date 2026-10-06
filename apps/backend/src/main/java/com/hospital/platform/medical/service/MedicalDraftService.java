package com.hospital.platform.medical.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.medical.repository.MedicalDraftRepository;
import com.hospital.platform.medical.repository.MedicalDraftRepository.DraftRow;
import com.hospital.platform.medical.repository.MedicalDraftSection;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalDraftService {

    private final MedicalContextService context;
    private final MedicalDraftRepository drafts;
    private final CapacityGateway capacity;
    private final AuditLogService audit;
    private final ObjectMapper json;
    private final Clock clock;

    public MedicalDraftService(MedicalContextService context, MedicalDraftRepository drafts,
                               CapacityGateway capacity, @Lazy AuditLogService audit,
                               ObjectMapper json, Clock clock) {
        this.context = context;
        this.drafts = drafts;
        this.capacity = capacity;
        this.audit = audit;
        this.json = json;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public <T> DraftResult<T> read(UUID encounterId, MedicalDraftSection section, Class<T> type) {
        UUID professionalId = context.requireCurrentProfessionalId();
        return response(editable(encounterId, professionalId, section), type);
    }

    @Transactional
    public <T> DraftResult<T> save(UUID encounterId, int expectedVersion, MedicalDraftSection section,
                                   T value, Class<T> type) {
        return save(encounterId, expectedVersion, section, value, type, () -> { });
    }

    @Transactional
    public <T> DraftResult<T> save(UUID encounterId, int expectedVersion, MedicalDraftSection section,
                                   T value, Class<T> type, Runnable validateValue) {
        UUID professionalId = context.requireCurrentProfessionalId();
        DraftRow current = editable(encounterId, professionalId, section);
        capacity.lockAppointment(current.appointmentId());
        current = editable(encounterId, professionalId, section);

        int version = current.version() == null ? 0 : current.version();
        if (expectedVersion != version) {
            throw new MedicalDraftVersionConflictException();
        }
        validateValue.run();
        String contentJson = write(value);
        int saved = version == 0
                ? drafts.insertSection(encounterId, section, contentJson, clock.instant())
                : drafts.updateSection(encounterId, version, section, contentJson, clock.instant());
        if (saved != 1) {
            throw new MedicalDraftVersionConflictException();
        }
        audit.record(AuditEventType.CLINICAL_DRAFT_SAVED, "ClinicalEncounter", encounterId,
                Map.of(), Map.of("version", version + 1, "section", section.key()));
        return response(editable(encounterId, professionalId, section), type);
    }

    private DraftRow editable(UUID encounterId, UUID professionalId, MedicalDraftSection section) {
        return drafts.findEditable(encounterId, professionalId, section)
                .orElseThrow(MedicalDraftNotFoundException::new);
    }

    private <T> DraftResult<T> response(DraftRow row, Class<T> type) {
        return new DraftResult<>(row.version() == null ? 0 : row.version(),
                read(row.sectionJson(), type), row.updatedAt());
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize clinical draft", exception);
        }
    }

    private <T> T read(String value, Class<T> type) {
        if (value == null) {
            return null;
        }
        try {
            return json.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot read clinical draft", exception);
        }
    }

    public record DraftResult<T>(int version, T value, Instant updatedAt) {
    }
}
