package com.hospital.platform.medical.service;

import com.hospital.platform.medical.dto.MedicalHistoryDraftDTO;
import com.hospital.platform.medical.dto.MedicalHistoryDraftResponseDTO;
import com.hospital.platform.medical.dto.MedicalHistoryDraftUpdateDTO;
import com.hospital.platform.medical.repository.MedicalDraftSection;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MedicalHistoryDraftService {

    private final MedicalDraftService drafts;

    public MedicalHistoryDraftService(MedicalDraftService drafts) {
        this.drafts = drafts;
    }

    public MedicalHistoryDraftResponseDTO read(UUID encounterId) {
        return response(encounterId, drafts.read(encounterId, MedicalDraftSection.HISTORY,
                MedicalHistoryDraftDTO.class));
    }

    public MedicalHistoryDraftResponseDTO save(UUID encounterId, MedicalHistoryDraftUpdateDTO request) {
        return response(encounterId, drafts.save(encounterId, request.version(), MedicalDraftSection.HISTORY,
                request.history(), MedicalHistoryDraftDTO.class));
    }

    private MedicalHistoryDraftResponseDTO response(UUID encounterId,
            MedicalDraftService.DraftResult<MedicalHistoryDraftDTO> result) {
        return new MedicalHistoryDraftResponseDTO(encounterId, result.version(), result.value(), result.updatedAt());
    }
}
