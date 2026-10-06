package com.hospital.platform.medical.service;

import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO.Diagnosis;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO.ReferralType;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftDTO.TreatmentPlan;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftResponseDTO;
import com.hospital.platform.medical.dto.MedicalAssessmentDraftUpdateDTO;
import com.hospital.platform.medical.repository.MedicalAssessmentReferenceRepository;
import com.hospital.platform.medical.repository.MedicalDraftSection;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MedicalAssessmentDraftService {

    private final MedicalDraftService drafts;
    private final MedicalAssessmentReferenceRepository references;

    public MedicalAssessmentDraftService(MedicalDraftService drafts, MedicalAssessmentReferenceRepository references) {
        this.drafts = drafts;
        this.references = references;
    }

    public MedicalAssessmentDraftResponseDTO read(UUID encounterId) {
        return response(encounterId, drafts.read(encounterId, MedicalDraftSection.ASSESSMENT,
                MedicalAssessmentDraftDTO.class));
    }

    public MedicalAssessmentDraftResponseDTO save(UUID encounterId, MedicalAssessmentDraftUpdateDTO request) {
        return response(encounterId, drafts.save(encounterId, request.version(), MedicalDraftSection.ASSESSMENT,
                request.assessment(), MedicalAssessmentDraftDTO.class,
                () -> validateReferences(request.assessment())));
    }

    private void validateReferences(MedicalAssessmentDraftDTO assessment) {
        Diagnosis diagnosis = assessment.diagnosis();
        if (diagnosis != null) {
            if (diagnosis.icd10CodeId() != null && !references.activeIcd10(diagnosis.icd10CodeId())) {
                throw new InvalidMedicalAssessmentDraftException();
            }
            if (diagnosis.procedureId() != null && !references.activeProcedure(diagnosis.procedureId())) {
                throw new InvalidMedicalAssessmentDraftException();
            }
        }
        TreatmentPlan plan = assessment.treatmentPlan();
        if (plan != null && plan.referralSpecialtyId() != null) {
            if (plan.referralType() != ReferralType.INTERCONSULTATION
                    || !references.activeSpecialty(plan.referralSpecialtyId())) {
                throw new InvalidMedicalAssessmentDraftException();
            }
        }
    }

    private MedicalAssessmentDraftResponseDTO response(UUID encounterId,
            MedicalDraftService.DraftResult<MedicalAssessmentDraftDTO> result) {
        return new MedicalAssessmentDraftResponseDTO(encounterId, result.version(),
                result.value(), result.updatedAt());
    }
}
