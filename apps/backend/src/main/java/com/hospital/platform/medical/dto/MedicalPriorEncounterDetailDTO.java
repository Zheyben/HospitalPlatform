package com.hospital.platform.medical.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record MedicalPriorEncounterDetailDTO(
        PatientEncounterDetailDTO encounter,
        JsonNode history,
        MedicalPriorOrderDTO order,
        PatientPrescriptionDetailDTO prescription
) {
}
