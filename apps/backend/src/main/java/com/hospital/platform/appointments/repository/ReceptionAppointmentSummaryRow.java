package com.hospital.platform.appointments.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface ReceptionAppointmentSummaryRow {
    UUID getAppointmentId();
    UUID getPatientId();
    String getDocumentType();
    String getDocumentNumber();
    String getPatientName();
    String getProfessionalName();
    String getSpecialtyName();
    LocalDate getAppointmentDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
    String getStatus();
    String getFlowStage();
}
