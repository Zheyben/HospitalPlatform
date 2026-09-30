package com.hospital.platform.professionals.mapper;

import com.hospital.platform.professionals.dto.ProfessionalResponseDTO;
import com.hospital.platform.professionals.entity.Professional;
import java.util.List;

public class ProfessionalMapper {

    public ProfessionalResponseDTO toResponse(Professional professional) {
        return new ProfessionalResponseDTO(
                professional.getId(),
                professional.getUserId(),
                professional.getLicenseNumber(),
                professional.isActive()
        );
    }

    public List<ProfessionalResponseDTO> toResponseList(List<Professional> professionals) {
        return professionals.stream()
                .map(this::toResponse)
                .toList();
    }
}
