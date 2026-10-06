package com.hospital.platform.appointments.repository;

import java.time.LocalTime;
import java.util.UUID;

public interface ReceptionWaitingRoomRow {
    UUID getAppointmentId();
    String getPatientDisplay();
    LocalTime getStartTime();
    String getProfessionalName();
    String getSpecialtyName();
    String getFlowStage();
}
