package com.hospital.platform.medical.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.platform.medical.dto.MedicalAppointmentContextDTO;
import com.hospital.platform.medical.dto.MedicalPriorEncounterDetailDTO;
import com.hospital.platform.medical.dto.PatientEncounterDetailDTO;
import com.hospital.platform.medical.dto.PatientPrescriptionDetailDTO;
import com.hospital.platform.medical.repository.MedicalLongitudinalRepository;
import com.hospital.platform.medical.repository.PatientClinicalReadRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalLongitudinalService {

    private final MedicalContextService context;
    private final MedicalLongitudinalRepository records;
    private final PatientClinicalReadRepository patientRecords;

    public MedicalLongitudinalService(MedicalContextService context,
            MedicalLongitudinalRepository records, PatientClinicalReadRepository patientRecords) {
        this.context = context;
        this.records = records;
        this.patientRecords = patientRecords;
    }

    @Transactional(readOnly = true)
    public MedicalPriorEncounterDetailDTO detail(UUID appointmentId, UUID priorEncounterId) {
        MedicalAppointmentContextDTO current = context.appointmentContext(appointmentId);
        UUID patientId = current.patientId();
        if (!records.belongsToPriorFinalizedAppointment(appointmentId, patientId, priorEncounterId)) {
            throw new MedicalAppointmentNotFoundException();
        }
        PatientEncounterDetailDTO encounter = patientRecords.encounter(patientId, priorEncounterId)
                .orElseThrow(MedicalAppointmentNotFoundException::new);
        JsonNode history = records.history(priorEncounterId).orElse(null);
        PatientPrescriptionDetailDTO prescription = records.prescriptionId(priorEncounterId)
                .flatMap(id -> patientRecords.prescription(patientId, id)).orElse(null);
        return new MedicalPriorEncounterDetailDTO(encounter, history,
                records.order(priorEncounterId).orElse(null), prescription);
    }
}
