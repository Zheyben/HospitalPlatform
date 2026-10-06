package com.hospital.platform.medical.service;

import com.hospital.platform.medical.dto.MedicalPrescriptionDraftDTO;
import com.hospital.platform.medical.dto.MedicalPrescriptionDraftResponseDTO;
import com.hospital.platform.medical.dto.MedicalPrescriptionDraftUpdateDTO;
import com.hospital.platform.medical.repository.MedicalDraftSection;
import com.hospital.platform.medical.repository.MedicalPrescriptionReferenceRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MedicalPrescriptionDraftService {

    private final MedicalDraftService drafts;
    private final MedicalPrescriptionReferenceRepository references;

    public MedicalPrescriptionDraftService(MedicalDraftService drafts,
                                           MedicalPrescriptionReferenceRepository references) {
        this.drafts = drafts;
        this.references = references;
    }

    public MedicalPrescriptionDraftResponseDTO read(UUID encounterId) {
        return response(encounterId, drafts.read(encounterId, MedicalDraftSection.PRESCRIPTION,
                MedicalPrescriptionDraftDTO.class));
    }

    public MedicalPrescriptionDraftResponseDTO save(UUID encounterId, MedicalPrescriptionDraftUpdateDTO request) {
        return response(encounterId, drafts.save(encounterId, request.version(), MedicalDraftSection.PRESCRIPTION,
                request.prescription(), MedicalPrescriptionDraftDTO.class,
                () -> validateReferences(request.prescription())));
    }

    private void validateReferences(MedicalPrescriptionDraftDTO prescription) {
        for (MedicalPrescriptionDraftDTO.MedicationItem item : prescription.items()) {
            if (!references.activePresentationForMedication(item.medicationId(), item.presentationId())) {
                throw new InvalidMedicalPrescriptionDraftException();
            }
        }
    }

    private MedicalPrescriptionDraftResponseDTO response(UUID encounterId,
            MedicalDraftService.DraftResult<MedicalPrescriptionDraftDTO> result) {
        return new MedicalPrescriptionDraftResponseDTO(encounterId, result.version(),
                result.value(), result.updatedAt());
    }
}
