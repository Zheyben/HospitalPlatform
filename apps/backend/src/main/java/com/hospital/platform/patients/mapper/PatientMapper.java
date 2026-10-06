package com.hospital.platform.patients.mapper;

import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.entity.Patient;
import com.hospital.platform.patients.domain.PatientDemographics;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponseDTO toResponse(Patient patient) {
        PatientDemographics demographics = patient.getDemographics();
        return new PatientResponseDTO(
                patient.getId(),
                patient.getUserId(),
                patient.getDocumentType(),
                patient.getDocumentNumber(),
                patient.getBirthDate(),
                patient.getPhone(),
                patient.getInsurance(),
                patient.getInsuranceId(),
                patient.getAddress(),
                patient.getSex(),
                patient.isActive(),
                demographics.maritalStatus(), demographics.occupation(), demographics.district(),
                demographics.educationLevel(), demographics.affiliationNumber(),
                demographics.emergencyContactName(), demographics.emergencyContactRelationship(),
                demographics.emergencyContactPhone()
        );
    }

    public List<PatientResponseDTO> toResponseList(List<Patient> patients) {
        return patients.stream()
                .map(this::toResponse)
                .toList();
    }
}
