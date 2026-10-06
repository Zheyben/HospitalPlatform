package com.hospital.platform.agenda.contract;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCapacityGateway implements CapacityGateway {
    private final JdbcTemplate jdbc;
    private final EntityManager entityManager;

    public PostgresCapacityGateway(JdbcTemplate jdbc, EntityManager entityManager) {
        this.jdbc = jdbc;
        this.entityManager = entityManager;
    }

    private void before() {
        entityManager.flush();
        entityManager.clear();
    }

    private void after() {
        entityManager.clear();
    }

    private UUID uuid(String sql, Object... args) {
        before();
        UUID result = jdbc.queryForObject(sql, UUID.class, args);
        after();
        return result;
    }

    private void call(String sql, Object... args) {
        before();
        jdbc.queryForObject(sql, Object.class, args);
        after();
    }

    @Override public boolean isOperationalAssociation(UUID professionalId, UUID specialtyId) {
        Boolean result = jdbc.queryForObject("""
                select exists(select 1 from professional_specialties ps
                  join professionals p on p.id=ps.professional_id
                  join specialties sp on sp.id=ps.specialty_id
                  left join users u on u.id=p.user_id
                  where ps.professional_id=? and ps.specialty_id=?
                    and p.deleted_at is null and sp.active and sp.deleted_at is null
                    and (p.user_id is null or (u.enabled and u.deleted_at is null)))
                """, Boolean.class, professionalId, specialtyId);
        return Boolean.TRUE.equals(result);
    }

    @Override public boolean isFutureSlot(UUID slotId) {
        Boolean result = jdbc.queryForObject("""
                select exists(select 1 from availability_slots
                  where id=? and slot_date + start_time > hospital_business_now())
                """, Boolean.class, slotId);
        return Boolean.TRUE.equals(result);
    }

    @Override public void lockAppointment(UUID appointmentId) {
        call("select capacity_lock_appointment(?)", appointmentId);
    }
    @Override public void lockReschedule(UUID appointmentId, UUID newSlotId) {
        call("select capacity_lock_reschedule(?, ?)", appointmentId, newSlotId);
    }

    @Override public UUID reserve(UUID slotId, UUID patientId, String reason) {
        return uuid("select capacity_reserve(?, ?, ?)", slotId, patientId, reason);
    }
    @Override public void cancel(UUID appointmentId, UUID actorUserId) {
        call("select capacity_cancel(?, ?)", appointmentId, actorUserId);
    }
    @Override public UUID reschedule(UUID appointmentId, UUID newSlotId) {
        return uuid("select capacity_reschedule(?, ?)", appointmentId, newSlotId);
    }
    @Override public void confirm(UUID appointmentId) {
        call("select capacity_appointment_confirm(?)", appointmentId);
    }
    @Override public void stage(UUID appointmentId, String stage) {
        call("select capacity_appointment_stage(?, ?)", appointmentId, stage);
    }
    @Override public void complete(UUID appointmentId) {
        call("select capacity_appointment_complete(?)", appointmentId);
    }
    @Override public UUID createSchedule(UUID professionalId, UUID specialtyId, int dayOfWeek,
                                          LocalTime startTime, LocalTime endTime) {
        return uuid("select capacity_schedule_create(?, ?, ?, cast(? as time), cast(? as time))", professionalId,
                specialtyId, dayOfWeek, startTime.toString(), endTime.toString());
    }
    @Override public void reconfigureSchedule(UUID scheduleId, UUID professionalId, UUID specialtyId,
                                               int dayOfWeek, LocalTime startTime, LocalTime endTime) {
        call("select capacity_schedule_reconfigure(?, ?, ?, ?, cast(? as time), cast(? as time))",
                scheduleId, professionalId, specialtyId, dayOfWeek, startTime.toString(), endTime.toString());
    }
    @Override public void scheduleStatus(UUID scheduleId, boolean active) {
        call("select capacity_schedule_status(?, ?)", scheduleId, active);
    }
    @Override public boolean generateSlot(UUID scheduleId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        before();
        Boolean created = jdbc.queryForObject(
                "select capacity_generate_slot_created(?, ?, cast(? as time), cast(? as time))",
                Boolean.class, scheduleId, date, startTime.toString(), endTime.toString());
        after();
        return Boolean.TRUE.equals(created);
    }
    @Override public void deactivateProfessional(UUID professionalId) {
        call("select capacity_professional_deactivate(?)", professionalId);
    }
    @Override public void reactivateProfessional(UUID professionalId) {
        call("select capacity_professional_reactivate(?)", professionalId);
    }
    @Override public void disableUser(UUID userId) {
        call("select capacity_user_disable(?)", userId);
    }
    @Override public void setUserEnabled(UUID userId, boolean enabled) {
        call("select capacity_user_status(?, ?)", userId, enabled);
    }
    @Override public void lockUser(UUID userId) {
        call("select capacity_lock_user(?)", userId);
    }
}
