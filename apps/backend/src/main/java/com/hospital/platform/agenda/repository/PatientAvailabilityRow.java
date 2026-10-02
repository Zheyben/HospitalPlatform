package com.hospital.platform.agenda.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface PatientAvailabilityRow {
    UUID getId();
    UUID getScheduleId();
    LocalDate getSlotDate();
    LocalTime getStartTime();
    LocalTime getEndTime();
    String getStatus();
    UUID getProfessionalId();
    String getProfessionalName();
    UUID getSpecialtyId();
    String getSpecialtyName();
}
