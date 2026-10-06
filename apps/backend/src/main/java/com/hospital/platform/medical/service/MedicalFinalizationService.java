package com.hospital.platform.medical.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.platform.agenda.contract.CapacityGateway;
import com.hospital.platform.appointments.service.AppointmentService;
import com.hospital.platform.audit.contract.AuditEventType;
import com.hospital.platform.audit.contract.AuditLogService;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO.Diagnosis;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO.ReferralType;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO.TreatmentPlan;
import com.hospital.platform.medical.dto.MedicalFinalizationResponseDTO;
import com.hospital.platform.medical.dto.MedicalHistoryDraftDTO;
import com.hospital.platform.medical.dto.MedicalPrescriptionDraftDTO;
import com.hospital.platform.medical.repository.MedicalFinalizationRepository;
import com.hospital.platform.medical.repository.MedicalFinalizationRepository.FinalizationRow;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalFinalizationService {

    private final MedicalContextService context;
    private final MedicalFinalizationRepository records;
    private final CapacityGateway capacity;
    private final AppointmentService appointments;
    private final AuditLogService audit;
    private final ObjectMapper json;
    private final Validator validator;
    private final Clock clock;

    public MedicalFinalizationService(MedicalContextService context, MedicalFinalizationRepository records,
                                      CapacityGateway capacity, AppointmentService appointments,
                                      @Lazy AuditLogService audit, ObjectMapper json,
                                      Validator validator, Clock clock) {
        this.context = context;
        this.records = records;
        this.capacity = capacity;
        this.appointments = appointments;
        this.audit = audit;
        this.json = json;
        this.validator = validator;
        this.clock = clock;
    }

    @Transactional
    public MedicalFinalizationResponseDTO finalizeEncounter(UUID encounterId, int expectedVersion) {
        UUID professionalId = context.requireCurrentProfessionalId();
        UUID appointmentId = records.appointmentId(encounterId)
                .orElseThrow(MedicalDraftNotFoundException::new);
        capacity.lockAppointment(appointmentId);
        FinalizationRow row = records.find(encounterId, professionalId)
                .orElseThrow(MedicalDraftNotFoundException::new);
        String hash = hash(row.content());
        if ("FINALIZED".equals(row.status())) {
            if (row.finalVersion() != null && row.finalVersion() == expectedVersion
                    && hash.equals(row.finalHash())
                    && "COMPLETED".equals(row.appointmentStatus())
                    && "FINISHED".equals(row.flowStage())) {
                return response(encounterId, row);
            }
            throw new MedicalFinalizationConflictException();
        }
        if (!"OPEN".equals(row.status()) || !"CONFIRMED".equals(row.appointmentStatus())
                || !"IN_ATTENTION".equals(row.flowStage())
                || row.version() == null || row.version() != expectedVersion) {
            throw new MedicalFinalizationConflictException();
        }

        DraftContent content = readAndValidate(row.content());
        Instant finalizedAt = clock.instant();
        if (records.insertRecord(encounterId, finalizedAt, expectedVersion, hash) != 1) {
            throw new MedicalFinalizationConflictException();
        }
        records.insertHistory(encounterId);
        records.insertAssessment(encounterId);
        records.insertDiagnosis(encounterId);
        records.insertTreatment(encounterId);
        if (content.assessment().diagnosis().procedureId() != null) {
            records.insertOrder(encounterId, finalizedAt);
        }
        if (content.prescription() != null && !content.prescription().items().isEmpty()) {
            UUID prescriptionId = records.insertPrescription(encounterId, finalizedAt);
            for (int i = 0; i < content.prescription().items().size(); i++) {
                MedicalPrescriptionDraftDTO.MedicationItem item = content.prescription().items().get(i);
                if (records.insertPrescriptionItem(prescriptionId, i + 1, item.medicationId(),
                        item.presentationId(), encounterId) != 1) {
                    throw new InvalidMedicalFinalizationException();
                }
            }
        }
        if (records.markFinalized(encounterId, finalizedAt) != 1) {
            throw new MedicalFinalizationConflictException();
        }
        appointments.completeMedicalAppointment(appointmentId);
        audit.record(AuditEventType.CLINICAL_ENCOUNTER_FINALIZED, "ClinicalEncounter", encounterId,
                Map.of(), Map.of("appointmentId", appointmentId.toString(), "version", expectedVersion));
        return response(encounterId, records.find(encounterId, professionalId)
                .orElseThrow(MedicalDraftNotFoundException::new));
    }

    private DraftContent readAndValidate(String raw) {
        try {
            JsonNode content = json.readTree(raw);
            MedicalHistoryDraftDTO history = section(content, "history", MedicalHistoryDraftDTO.class);
            MedicalAssessmentDraftDTO assessment = section(content, "assessment", MedicalAssessmentDraftDTO.class);
            MedicalPrescriptionDraftDTO prescription = section(content, "prescription", MedicalPrescriptionDraftDTO.class);
            if (history == null || assessment == null || assessment.presentation() == null
                    || assessment.diagnosis() == null || assessment.treatmentPlan() == null) {
                throw new InvalidMedicalFinalizationException();
            }
            validateBean(history);
            validateBean(assessment);
            if (prescription != null) {
                validateBean(prescription);
            }
            if (!filled(assessment.presentation().reason())
                    || !filled(assessment.presentation().symptomsAndCurrentIllness())
                    || (assessment.presentation().illnessDuration() == null)
                            != (assessment.presentation().illnessDurationUnit() == null)) {
                throw new InvalidMedicalFinalizationException();
            }
            Diagnosis diagnosis = assessment.diagnosis();
            TreatmentPlan plan = assessment.treatmentPlan();
            if (!filled(diagnosis.primaryDiagnosis()) || diagnosis.icd10CodeId() == null
                    || diagnosis.diagnosisType() == null || !filled(plan.therapeuticPlan())
                    || !filled(plan.generalIndications()) || !records.lockActiveIcd10(diagnosis.icd10CodeId())) {
                throw new InvalidMedicalFinalizationException();
            }
            if (diagnosis.priority() != null && diagnosis.procedureId() == null) {
                throw new InvalidMedicalFinalizationException();
            }
            if (diagnosis.procedureId() != null && !records.lockActiveProcedure(diagnosis.procedureId())) {
                throw new InvalidMedicalFinalizationException();
            }
            if (plan.referralType() == ReferralType.INTERCONSULTATION) {
                if (plan.referralSpecialtyId() == null
                        || !records.lockActiveSpecialty(plan.referralSpecialtyId())) {
                    throw new InvalidMedicalFinalizationException();
                }
            } else if (plan.referralSpecialtyId() != null) {
                throw new InvalidMedicalFinalizationException();
            }
            if (prescription != null) {
                for (MedicalPrescriptionDraftDTO.MedicationItem item : prescription.items()) {
                    if (!records.lockActivePresentation(item.medicationId(), item.presentationId())) {
                        throw new InvalidMedicalFinalizationException();
                    }
                }
            }
            return new DraftContent(assessment, prescription);
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            throw new InvalidMedicalFinalizationException();
        }
    }

    private <T> T section(JsonNode content, String name, Class<T> type) throws JsonProcessingException {
        JsonNode value = content.get(name);
        return value == null || value.isNull() ? null : json.treeToValue(value, type);
    }

    private <T> void validateBean(T value) {
        if (!validator.validate(value).isEmpty()) {
            throw new InvalidMedicalFinalizationException();
        }
    }

    private boolean filled(String value) {
        return value != null && !value.isBlank();
    }

    private String hash(String content) {
        if (content == null) {
            return "";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private MedicalFinalizationResponseDTO response(UUID encounterId, FinalizationRow row) {
        return new MedicalFinalizationResponseDTO(encounterId, row.appointmentId(),
                row.status(), row.finalizedAt(), row.prescriptionNumber());
    }

    private record DraftContent(MedicalAssessmentDraftDTO assessment, MedicalPrescriptionDraftDTO prescription) {
    }
}
