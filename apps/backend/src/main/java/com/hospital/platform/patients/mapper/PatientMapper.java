package com.hospital.platform.patients.mapper;

import com.hospital.platform.patients.dto.PatientResponseDTO;
import com.hospital.platform.patients.entity.Patient;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponseDTO toResponse(Patient patient) {
        return new PatientResponseDTO(
                patient.getId(),
                patient.getUserId(),
                patient.getDocumentType(),
                patient.getDocumentNumber(),
                patient.getBirthDate(),
                patient.getPhone(),
                patient.getAddress(),
                patient.isActive()
        );
    }

    public List<PatientResponseDTO> toResponseList(List<Patient> patients) {
        return patients.stream()
                .map(this::toResponse)
                .toList();
    }
}
