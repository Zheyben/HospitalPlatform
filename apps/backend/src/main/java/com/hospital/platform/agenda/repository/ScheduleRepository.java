package com.hospital.platform.agenda.repository;

import com.hospital.platform.agenda.entity.Schedule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    @Query("""
            select schedule
            from Schedule schedule
            where (:professionalId is null or schedule.professionalId = :professionalId)
              and (:specialtyId is null or schedule.specialtyId = :specialtyId)
            order by schedule.createdAt desc
            """)
    List<Schedule> findSchedules(
            @Param("professionalId") UUID professionalId,
            @Param("specialtyId") UUID specialtyId
    );
}
