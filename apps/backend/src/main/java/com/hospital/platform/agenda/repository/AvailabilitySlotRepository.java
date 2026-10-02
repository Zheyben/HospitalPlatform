package com.hospital.platform.agenda.repository;

import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {

    boolean existsByIdAndStatus(UUID id, AvailabilitySlotStatus status);

    @Query("""
            select slot
            from AvailabilitySlot slot
            join fetch slot.schedule
            where slot.id = :slotId
            """)
    Optional<AvailabilitySlot> findByIdWithSchedule(@Param("slotId") UUID slotId);

    @Query(value = """
            select slot.id as id, slot.schedule_id as scheduleId, slot.slot_date as slotDate,
                   slot.start_time as startTime, slot.end_time as endTime, slot.status as status,
                   s.professional_id as professionalId, s.specialty_id as specialtyId,
                   coalesce(nullif(trim(concat(coalesce(u.first_name, ''), ' ',
                       coalesce(u.last_name, ''))), ''), p.license_number) as professionalName,
                   sp.name as specialtyName
            from availability_slots slot
            join schedules s on s.id = slot.schedule_id and s.active = true
            join professionals p on p.id = s.professional_id and p.deleted_at is null
            left join users u on u.id = p.user_id and u.deleted_at is null
            join specialties sp on sp.id = s.specialty_id and sp.active = true and sp.deleted_at is null
            where slot.status = 'AVAILABLE'
              and (p.user_id is null or u.enabled = true)
              and (slot.slot_date > :today or (slot.slot_date = :today and slot.start_time > :nowTime))
              and (cast(:scheduleId as uuid) is null or s.id = :scheduleId)
              and (cast(:professionalId as uuid) is null or s.professional_id = :professionalId)
              and (cast(:slotDate as date) is null or slot.slot_date = :slotDate)
              and not exists (select 1 from appointments a where a.slot_id = slot.id
                              and a.appointment_status in ('SCHEDULED', 'CONFIRMED', 'COMPLETED'))
            order by slot.slot_date, slot.start_time, sp.name, professionalName
            """, nativeQuery = true)
    List<PatientAvailabilityRow> findPatientAvailability(@Param("scheduleId") UUID scheduleId,
            @Param("professionalId") UUID professionalId, @Param("slotDate") LocalDate slotDate,
            @Param("today") LocalDate today, @Param("nowTime") java.time.LocalTime nowTime);

    @Query("""
            select slot
            from AvailabilitySlot slot
            join fetch slot.schedule schedule
            where (:scheduleId is null or schedule.id = :scheduleId)
              and (:professionalId is null or schedule.professionalId = :professionalId)
              and (:slotDate is null or slot.slotDate = :slotDate)
              and (:status is null or slot.status = :status)
            order by slot.slotDate, slot.startTime
            """)
    List<AvailabilitySlot> findAvailability(
            @Param("scheduleId") UUID scheduleId,
            @Param("professionalId") UUID professionalId,
            @Param("slotDate") LocalDate slotDate,
            @Param("status") AvailabilitySlotStatus status
    );
}
