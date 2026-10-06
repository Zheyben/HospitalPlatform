package com.hospital.platform.patients.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePatientDemographicsRequestDTO(
        @Size(max = 80) String maritalStatus,
        @Size(max = 120) String occupation,
        @Size(max = 120) String district,
        @Size(max = 120) String educationLevel,
        @Size(max = 60) String affiliationNumber,
        @Size(max = 150) String emergencyContactName,
        @Size(max = 80) String emergencyContactRelationship,
        @Size(max = 50) @Pattern(regexp = "^\\+?[0-9]{7,15}$") String emergencyContactPhone
) {
}
