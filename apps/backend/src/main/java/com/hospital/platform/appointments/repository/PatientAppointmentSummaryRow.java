package com.hospital.platform.appointments.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface PatientAppointmentSummaryRow {
    UUID getAppointmentId();
    String getStatus();
    String getFlowStage();
    String getReason();
    UUID getSpecialtyId();
    String getSpecialtyName();
    UUID getProfessionalId();
    String getProfessionalName();
    UUID getSlotId();
    LocalDate getAppointmentDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
}
