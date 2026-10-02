package com.hospital.platform.auth.service;

import com.hospital.platform.auth.dto.RegisterPatientRequestDTO;
import com.hospital.platform.auth.dto.RegisterPatientResponseDTO;
import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.service.PatientService;
import com.hospital.platform.users.entity.User;
import com.hospital.platform.users.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientRegistrationService {

    private final UserService userService;
    private final PatientService patientService;

    public PatientRegistrationService(UserService userService, PatientService patientService) {
        this.userService = userService;
        this.patientService = patientService;
    }

    @Transactional
    public RegisterPatientResponseDTO register(RegisterPatientRequestDTO request) {
        User user = userService.registerPatientUser(
                request.email(), request.password(), request.firstName(), request.lastName()
        );
        PatientResponseDTO patient = patientService.registerPatient(
                user.getId(), request.documentType(), request.documentNumber(),
                request.birthDate(), request.phone(), request.insurance()
        );
        return new RegisterPatientResponseDTO(
                user.getId(), patient.id(), user.getEmail(), user.getFirstName(), user.getLastName(),
                patient.documentType(), patient.documentNumber(), patient.birthDate(),
                patient.phone(), patient.insurance()
        );
    }
}
