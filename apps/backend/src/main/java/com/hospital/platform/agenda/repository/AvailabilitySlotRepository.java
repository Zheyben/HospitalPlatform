package com.hospital.platform.agenda.repository;

import com.hospital.platform.agenda.entity.AvailabilitySlot;
import com.hospital.platform.agenda.entity.AvailabilitySlotStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            update availability_slots slot
            set status = 'RESERVED',
                updated_at = current_timestamp
            where slot.id = :slotId
              and slot.status = 'AVAILABLE'
              and exists (
                  select 1
                  from schedules schedule
                  where schedule.id = slot.schedule_id
                    and schedule.active = true
              )
            """, nativeQuery = true)
    int reserveUsableSlot(@Param("slotId") UUID slotId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            update availability_slots
            set status = 'AVAILABLE',
                updated_at = current_timestamp
            where id = :slotId
              and status = 'RESERVED'
            """, nativeQuery = true)
    int releaseReservedSlot(@Param("slotId") UUID slotId);

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
