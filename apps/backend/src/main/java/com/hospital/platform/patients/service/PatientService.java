package com.hospital.platform.patients.service;

import com.hospital.platform.patients.dto.CreatePatientRequestDTO;
import com.hospital.platform.patients.dto.LinkUserRequestDTO;
import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.dto.UpdatePatientRequestDTO;
import com.hospital.platform.patients.dto.UpdatePatientStatusRequestDTO;
import com.hospital.platform.patients.entity.Patient;
import com.hospital.platform.patients.exception.DuplicateDocumentException;
import com.hospital.platform.patients.exception.PatientAlreadyLinkedException;
import com.hospital.platform.patients.exception.PatientNotFoundException;
import com.hospital.platform.patients.exception.UserAlreadyLinkedException;
import com.hospital.platform.patients.exception.UserNotFoundException;
import com.hospital.platform.patients.mapper.PatientMapper;
import com.hospital.platform.patients.repository.PatientRepository;
import com.hospital.platform.users.service.CurrentUserService;
import com.hospital.platform.users.service.UserLookupService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserLookupService userLookupService;
    private final CurrentUserService currentUserService;
    private final PatientMapper patientMapper;

    public PatientService(
            PatientRepository patientRepository,
            UserLookupService userLookupService,
            CurrentUserService currentUserService,
            PatientMapper patientMapper
    ) {
        this.patientRepository = patientRepository;
        this.userLookupService = userLookupService;
        this.currentUserService = currentUserService;
        this.patientMapper = patientMapper;
    }

    @Transactional
    public PatientResponseDTO createPatient(CreatePatientRequestDTO request) {
        String documentNumber = normalizeDocumentNumber(request.documentNumber());
        assertDocumentAvailable(documentNumber);

        Patient patient = new Patient(
                null,
                normalizeDocumentType(request.documentType()),
                documentNumber,
                request.birthDate(),
                normalizeNullable(request.phone()),
                normalizeNullable(request.address())
        );

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> findPatients() {
        return patientMapper.toResponseList(patientRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public PatientResponseDTO findPatientById(UUID patientId) {
        return patientMapper.toResponse(findActivePatient(patientId));
    }

    @Transactional(readOnly = true)
    public PatientResponseDTO findCurrentPatient() {
        UUID userId = currentUserService.currentUserId();
        return patientRepository.findByUserIdAndDeletedAtIsNull(userId)
                .map(patientMapper::toResponse)
                .orElseThrow(() -> new PatientNotFoundException("Patient profile not found for current user"));
    }

    @Transactional
    public PatientResponseDTO updatePatient(UUID patientId, UpdatePatientRequestDTO request) {
        Patient patient = findActivePatient(patientId);
        String documentNumber = normalizeDocumentNumber(request.documentNumber());

        if (!patient.getDocumentNumber().equalsIgnoreCase(documentNumber)
                && patientRepository.existsByDocumentNumberIgnoreCaseAndIdNot(documentNumber, patientId)) {
            throw new DuplicateDocumentException(documentNumber);
        }

        patient.updateAdministrativeInfo(
                normalizeDocumentType(request.documentType()),
                documentNumber,
                request.birthDate(),
                normalizeNullable(request.phone()),
                normalizeNullable(request.address())
        );

        return patientMapper.toResponse(patient);
    }

    @Transactional
    public PatientResponseDTO updateStatus(UUID patientId, UpdatePatientStatusRequestDTO request) {
        Patient patient = findActivePatient(patientId);

        patient.deactivate();
        return patientMapper.toResponse(patient);
    }

    @Transactional
    public PatientResponseDTO linkUser(UUID patientId, LinkUserRequestDTO request) {
        Patient patient = findActivePatient(patientId);
        UUID userId = request.userId();

        if (patient.getUserId() != null) {
            throw new PatientAlreadyLinkedException(patientId);
        }
        if (!userLookupService.existsActiveUser(userId)) {
            throw new UserNotFoundException(userId);
        }
        if (patientRepository.existsByUserIdAndIdNot(userId, patientId)) {
            throw new UserAlreadyLinkedException(userId);
        }

        patient.linkUser(userId);
        return patientMapper.toResponse(patient);
    }

    private Patient findActivePatient(UUID patientId) {
        return patientRepository.findByIdAndDeletedAtIsNull(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    private void assertDocumentAvailable(String documentNumber) {
        if (patientRepository.existsByDocumentNumberIgnoreCase(documentNumber)) {
            throw new DuplicateDocumentException(documentNumber);
        }
    }

    private String normalizeDocumentType(String documentType) {
        return documentType.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDocumentNumber(String documentNumber) {
        return documentNumber.trim();
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
