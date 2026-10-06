package com.hospital.platform.medical.service;

import com.hospital.platform.medical.dto.PatientClinicalPageDTO;
import com.hospital.platform.medical.dto.PatientEncounterDetailDTO;
import com.hospital.platform.medical.dto.PatientEncounterSummaryDTO;
import com.hospital.platform.medical.dto.PatientPrescriptionDetailDTO;
import com.hospital.platform.medical.dto.PatientPrescriptionSummaryDTO;
import com.hospital.platform.medical.repository.PatientClinicalReadRepository;
import com.hospital.platform.patients.contract.PatientLookupService;
import com.hospital.platform.patients.contract.PatientReference;
import com.hospital.platform.users.service.CurrentUserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientClinicalReadService {

    private static final int MAX_LIMIT = 50;
    private static final int MAX_OFFSET = 10_000;

    private final PatientClinicalReadRepository records;
    private final PatientLookupService patients;
    private final CurrentUserService currentUser;

    public PatientClinicalReadService(PatientClinicalReadRepository records,
                                      PatientLookupService patients, CurrentUserService currentUser) {
        this.records = records;
        this.patients = patients;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public PatientClinicalPageDTO<PatientEncounterSummaryDTO> encounters(int limit, int offset) {
        UUID patientId = patientId();
        checkPage(limit, offset);
        return page(records.encounters(patientId, limit + 1, offset), limit, offset);
    }

    @Transactional(readOnly = true)
    public PatientEncounterDetailDTO encounter(UUID encounterId) {
        UUID patientId = patientId();
        return records.encounter(patientId, encounterId)
                .orElseThrow(PatientClinicalRecordNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public PatientClinicalPageDTO<PatientPrescriptionSummaryDTO> prescriptions(int limit, int offset) {
        UUID patientId = patientId();
        checkPage(limit, offset);
        return page(records.prescriptions(patientId, limit + 1, offset), limit, offset);
    }

    @Transactional(readOnly = true)
    public PatientPrescriptionDetailDTO prescription(UUID prescriptionId) {
        UUID patientId = patientId();
        return records.prescription(patientId, prescriptionId)
                .orElseThrow(PatientClinicalRecordNotFoundException::new);
    }

    private UUID patientId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> "ROLE_PATIENT".equals(authority.getAuthority()))) {
            throw new AccessDeniedException("Patient role required");
        }
        return patients.findActivePatientReferenceByUserId(currentUser.currentUserId())
                .map(PatientReference::id)
                .orElseThrow(() -> new AccessDeniedException("Active patient profile required"));
    }

    private void checkPage(int limit, int offset) {
        if (limit < 1 || limit > MAX_LIMIT || offset < 0 || offset > MAX_OFFSET) {
            throw new InvalidPatientClinicalPageException();
        }
    }

    private <T> PatientClinicalPageDTO<T> page(List<T> rows, int limit, int offset) {
        boolean hasMore = rows.size() > limit;
        return new PatientClinicalPageDTO<>(List.copyOf(rows.subList(0, Math.min(limit, rows.size()))),
                limit, offset, hasMore);
    }
}
