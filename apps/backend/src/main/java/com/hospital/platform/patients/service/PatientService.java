package com.hospital.platform.patients.service;

import com.hospital.platform.patients.dto.CreatePatientRequestDTO;
import com.hospital.platform.patients.dto.LinkUserRequestDTO;
import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.dto.ReceptionPatientSearchDTO;
import com.hospital.platform.patients.dto.UpdatePatientRequestDTO;
import com.hospital.platform.patients.dto.UpdatePatientDemographicsRequestDTO;
import com.hospital.platform.patients.dto.UpdatePatientStatusRequestDTO;
import com.hospital.platform.patients.domain.DocumentIdentity;
import com.hospital.platform.patients.domain.PatientDemographics;
import com.hospital.platform.patients.entity.Patient;
import com.hospital.platform.patients.exception.DuplicateDocumentException;
import com.hospital.platform.patients.exception.PatientAlreadyLinkedException;
import com.hospital.platform.patients.exception.PatientNotFoundException;
import com.hospital.platform.patients.exception.UserAlreadyLinkedException;
import com.hospital.platform.patients.exception.UserNotFoundException;
import com.hospital.platform.patients.mapper.PatientMapper;
import com.hospital.platform.patients.repository.PatientRepository;
import com.hospital.platform.patients.repository.ReceptionPatientSearchRow;
import com.hospital.platform.users.service.CurrentUserService;
import com.hospital.platform.users.service.UserLookupService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
        return createPatientRecord(
                request.documentType(), request.documentNumber(), request.birthDate(),
                request.phone(), request.address(), null, null, null, null, PatientDemographics.empty()
        );
    }

    @Transactional
    public PatientResponseDTO registerPatient(
            UUID userId,
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            String phone,
            String insurance,
            UUID insuranceId,
            String address,
            String sex
    ) {
        return registerPatient(userId, documentType, documentNumber, birthDate, phone, insurance,
                insuranceId, address, sex, PatientDemographics.empty());
    }

    @Transactional
    public PatientResponseDTO registerPatient(
            UUID userId, String documentType, String documentNumber, LocalDate birthDate,
            String phone, String insurance, UUID insuranceId, String address, String sex,
            PatientDemographics demographics
    ) {
        demographics.requireValidAffiliation(insurance);
        return createPatientRecord(
                documentType, documentNumber, birthDate, phone,
                address == null || address.isBlank() ? null : address, insurance, insuranceId, userId,
                sex == null || sex.isBlank() ? null : sex.trim(), demographics
        );
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> findPatients() {
        return patientMapper.toResponseList(patientRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public Optional<ReceptionPatientSearchDTO> findForReceptionByDocument(
            String documentType, String documentNumber
    ) {
        String type = DocumentIdentity.type(documentType);
        String number = DocumentIdentity.number(documentNumber);
        DocumentIdentity.requireValid(type, number);
        return patientRepository.findActiveForReceptionByDocument(type, number)
                .map(this::toReceptionSearch);
    }

    private ReceptionPatientSearchDTO toReceptionSearch(ReceptionPatientSearchRow row) {
        return new ReceptionPatientSearchDTO(
                row.getPatientId(), row.getDocumentType(), row.getDocumentNumber(), row.getPatientName()
        );
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
        String documentType = DocumentIdentity.type(request.documentType());
        String documentNumber = DocumentIdentity.number(request.documentNumber());
        DocumentIdentity.requireValid(documentType, documentNumber);

        if (patientRepository.existsByDocumentTypeAndDocumentNumberAndIdNot(documentType, documentNumber, patientId)) {
            throw new DuplicateDocumentException(documentNumber);
        }

        patient.updateAdministrativeInfo(
                documentType,
                documentNumber,
                request.birthDate(),
                normalizeNullable(request.phone()),
                normalizeNullable(request.address())
        );

        return patientMapper.toResponse(patient);
    }

    @Transactional
    public PatientResponseDTO updateDemographics(UUID patientId, UpdatePatientDemographicsRequestDTO request) {
        Patient patient = findActivePatient(patientId);
        PatientDemographics demographics = new PatientDemographics(
                request.maritalStatus(), request.occupation(), request.district(), request.educationLevel(),
                request.affiliationNumber(), request.emergencyContactName(),
                request.emergencyContactRelationship(), request.emergencyContactPhone());
        demographics.requireValidAffiliation(patient.getInsurance());
        patient.setDemographics(demographics);
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

    private void assertDocumentAvailable(String documentType, String documentNumber) {
        if (patientRepository.existsByDocumentTypeAndDocumentNumber(documentType, documentNumber)) {
            throw new DuplicateDocumentException(documentNumber);
        }
    }

    private PatientResponseDTO createPatientRecord(
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            String phone,
            String address,
            String insurance,
            UUID insuranceId,
            UUID userId,
            String sex,
            PatientDemographics demographics
    ) {
        String normalizedDocumentType = DocumentIdentity.type(documentType);
        String normalizedDocumentNumber = DocumentIdentity.number(documentNumber);
        DocumentIdentity.requireValid(normalizedDocumentType, normalizedDocumentNumber);
        assertDocumentAvailable(normalizedDocumentType, normalizedDocumentNumber);

        Patient patient = new Patient(
                null,
                normalizedDocumentType,
                normalizedDocumentNumber,
                birthDate,
                normalizeNullable(phone),
                normalizeNullable(address)
        );
        if (insuranceId != null) {
            patient.setInsurance(insuranceId, insurance);
        }
        if (sex != null) {
            patient.setSex(sex);
        }
        patient.setDemographics(demographics);
        if (userId != null) {
            patient.linkUser(userId);
        }
        return patientMapper.toResponse(
                userId == null ? patientRepository.save(patient) : patientRepository.saveAndFlush(patient)
        );
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
