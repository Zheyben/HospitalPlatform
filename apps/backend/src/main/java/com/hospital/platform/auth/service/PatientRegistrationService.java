package com.hospital.platform.auth.service;

import com.hospital.platform.auth.dto.RegisterPatientRequestDTO;
import com.hospital.platform.auth.dto.RegisterPatientResponseDTO;
import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.dto.InsuranceOptionDTO;
import com.hospital.platform.patients.domain.PatientDemographics;
import com.hospital.platform.patients.service.InsuranceCatalogService;
import com.hospital.platform.patients.service.PatientService;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientRegistrationService {

    private final UserService userService;
    private final PatientService patientService;
    private final InsuranceCatalogService insuranceCatalog;

    public PatientRegistrationService(UserService userService, PatientService patientService,
                                      InsuranceCatalogService insuranceCatalog) {
        this.userService = userService;
        this.patientService = patientService;
        this.insuranceCatalog = insuranceCatalog;
    }

    @Transactional
    public RegisterPatientResponseDTO register(RegisterPatientRequestDTO request) {
        InsuranceOptionDTO insurance = insuranceCatalog.resolve(request.insuranceId(), request.insurance());
        PatientDemographics demographics = new PatientDemographics(
                request.maritalStatus(), request.occupation(), request.district(), request.educationLevel(),
                request.affiliationNumber(), request.emergencyContactName(),
                request.emergencyContactRelationship(), request.emergencyContactPhone());
        demographics.requireValidAffiliation(insurance.code());
        User user = userService.registerPatientUser(
                request.email(), request.password(), request.firstName(), request.lastName()
        );
        PatientResponseDTO patient = patientService.registerPatient(
                user.getId(), request.documentType(), request.documentNumber(),
                request.birthDate(), request.phone(), insurance.name(), insurance.id(),
                request.address(), request.sex(), demographics);
        return new RegisterPatientResponseDTO(
                user.getId(), patient.id(), user.getEmail(), user.getFirstName(), user.getLastName(),
                patient.documentType(), patient.documentNumber(), patient.birthDate(),
                patient.phone(), patient.insurance(), patient.insuranceId(), patient.address(), patient.sex(),
                patient.maritalStatus(), patient.occupation(), patient.district(), patient.educationLevel(),
                patient.affiliationNumber(), patient.emergencyContactName(),
                patient.emergencyContactRelationship(), patient.emergencyContactPhone()
        );
    }
}
