package com.hospital.platform.appointments.repository;

import com.hospital.platform.appointments.entity.Appointment;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
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

    @Query(value = """
            select a.id as appointmentId, a.appointment_status as status,
                   a.flow_stage as flowStage, a.reason as reason,
                   sp.id as specialtyId, sp.name as specialtyName,
                   p.id as professionalId,
                   coalesce(nullif(trim(concat(coalesce(u.first_name, ''), ' ',
                       coalesce(u.last_name, ''))), ''), p.license_number) as professionalName,
                   slot.id as slotId, slot.slot_date as appointmentDate,
                   slot.start_time as startTime, slot.end_time as endTime
            from appointments a
            join availability_slots slot on slot.id = a.slot_id
            join schedules s on s.id = slot.schedule_id
            join specialties sp on sp.id = s.specialty_id
            join professionals p on p.id = a.professional_id
            left join users u on u.id = p.user_id
            where a.patient_id = :patientId
            order by a.created_at desc, a.id
            """, nativeQuery = true)
    List<PatientAppointmentSummaryRow> findPatientSummaries(@Param("patientId") UUID patientId);

    @Query(value = """
            select a.id as appointmentId, a.patient_id as patientId,
                   patient.document_type as documentType,
                   patient.document_number as documentNumber,
                   nullif(trim(concat(coalesce(patient_user.first_name, ''), ' ',
                       coalesce(patient_user.last_name, ''))), '') as patientName,
                   coalesce(nullif(trim(concat(coalesce(professional_user.first_name, ''), ' ',
                       coalesce(professional_user.last_name, ''))), ''), professional.license_number)
                       as professionalName,
                   specialty.name as specialtyName,
                   slot.slot_date as appointmentDate,
                   slot.start_time as startTime, slot.end_time as endTime,
                   a.appointment_status as status, a.flow_stage as flowStage
            from appointments a
            join patients patient on patient.id = a.patient_id
            left join users patient_user on patient_user.id = patient.user_id
            join professionals professional on professional.id = a.professional_id
            left join users professional_user on professional_user.id = professional.user_id
            join availability_slots slot on slot.id = a.slot_id
            join schedules schedule on schedule.id = slot.schedule_id
            join specialties specialty on specialty.id = schedule.specialty_id
            where a.patient_id = :patientId
            order by slot.slot_date desc, slot.start_time asc, a.id
            limit :limit
            """, nativeQuery = true)
    List<ReceptionAppointmentSummaryRow> findReceptionSummaries(
            @Param("patientId") UUID patientId,
            @Param("limit") int limit
    );

    @Query(value = """
            select slot.slot_date
            from appointments appointment
            join availability_slots slot on slot.id = appointment.slot_id
            where appointment.id = :appointmentId
            """, nativeQuery = true)
    LocalDate findSlotDateByAppointmentId(@Param("appointmentId") UUID appointmentId);

    @Query(value = """
            select a.id as appointmentId,
                   coalesce(nullif(trim(concat(coalesce(patient_user.first_name, ''), ' ',
                       coalesce(patient_user.last_name, ''))), ''),
                       patient.document_type || ' ' || patient.document_number) as patientDisplay,
                   slot.start_time as startTime,
                   coalesce(nullif(trim(concat(coalesce(professional_user.first_name, ''), ' ',
                       coalesce(professional_user.last_name, ''))), ''), professional.license_number)
                       as professionalName,
                   specialty.name as specialtyName, a.flow_stage as flowStage
            from appointments a
            join patients patient on patient.id = a.patient_id
            left join users patient_user on patient_user.id = patient.user_id
            join professionals professional on professional.id = a.professional_id
            left join users professional_user on professional_user.id = professional.user_id
            join availability_slots slot on slot.id = a.slot_id
            join schedules schedule on schedule.id = slot.schedule_id
            join specialties specialty on specialty.id = schedule.specialty_id
            where slot.slot_date = :today
              and a.appointment_status = 'CONFIRMED'
              and a.flow_stage in ('CHECK_IN', 'WAITING')
            order by slot.start_time, a.id
            limit :limit offset :offset
            """, nativeQuery = true)
    List<ReceptionWaitingRoomRow> findReceptionWaitingRoom(
            @Param("today") LocalDate today,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select appointment from Appointment appointment where appointment.id = :appointmentId")
    Optional<Appointment> findByIdForUpdate(@Param("appointmentId") UUID appointmentId);

    Optional<Appointment> findByRescheduledFromId(UUID appointmentId);
}
