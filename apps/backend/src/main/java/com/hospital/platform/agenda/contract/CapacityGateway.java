package com.hospital.platform.agenda.contract;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** Serialized PostgreSQL capacity mutations. Authorization belongs to application services. */
public interface CapacityGateway {
    boolean isOperationalAssociation(UUID professionalId, UUID specialtyId);
    boolean isFutureSlot(UUID slotId);
    void lockAppointment(UUID appointmentId);
    void lockReschedule(UUID appointmentId, UUID newSlotId);
    UUID reserve(UUID slotId, UUID patientId, String reason);
    void cancel(UUID appointmentId, UUID actorUserId);
    UUID reschedule(UUID appointmentId, UUID newSlotId);
    void confirm(UUID appointmentId);
    void stage(UUID appointmentId, String stage);
    void complete(UUID appointmentId);
    UUID createSchedule(UUID professionalId, UUID specialtyId, int dayOfWeek,
                        LocalTime startTime, LocalTime endTime);
    void reconfigureSchedule(UUID scheduleId, UUID professionalId, UUID specialtyId,
                             int dayOfWeek, LocalTime startTime, LocalTime endTime);
    void scheduleStatus(UUID scheduleId, boolean active);
    boolean generateSlot(UUID scheduleId, LocalDate date, LocalTime startTime, LocalTime endTime);
    void deactivateProfessional(UUID professionalId);
    void reactivateProfessional(UUID professionalId);
    void disableUser(UUID userId);
    void setUserEnabled(UUID userId, boolean enabled);
    void lockUser(UUID userId);
}
