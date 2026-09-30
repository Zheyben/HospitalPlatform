package com.hospital.platform.appointments.repository;

import com.hospital.platform.appointments.entity.Appointment;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findAllByOrderByCreatedAtDesc();

    List<Appointment> findAllByPatientIdOrderByCreatedAtDesc(UUID patientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select appointment from Appointment appointment where appointment.id = :appointmentId")
    Optional<Appointment> findByIdForUpdate(@Param("appointmentId") UUID appointmentId);

    Optional<Appointment> findByRescheduledFromId(UUID appointmentId);
}
