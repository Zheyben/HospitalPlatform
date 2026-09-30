package com.hospital.platform.appointments.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "slot_id", nullable = false)
    private UUID slotId;

    @Column(name = "rescheduled_from_id")
    private UUID rescheduledFromId;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_status", nullable = false, length = 30)
    private AppointmentStatus appointmentStatus = AppointmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_stage", length = 30)
    private FlowStage flowStage;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private UUID cancelledBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Appointment() {
    }

    public Appointment(UUID id, UUID patientId, UUID professionalId, UUID slotId, String reason) {
        this(id, patientId, professionalId, slotId, reason, null);
    }

    public Appointment(
            UUID id,
            UUID patientId,
            UUID professionalId,
            UUID slotId,
            String reason,
            UUID rescheduledFromId
    ) {
        this.id = id;
        this.patientId = patientId;
        this.professionalId = professionalId;
        this.slotId = slotId;
        this.reason = reason;
        this.rescheduledFromId = rescheduledFromId;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (appointmentStatus == null) {
            appointmentStatus = AppointmentStatus.SCHEDULED;
        }
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        touch();
    }

    public void confirm() {
        appointmentStatus = AppointmentStatus.CONFIRMED;
        touch();
    }

    public void cancel(LocalDateTime cancelledAt, UUID cancelledBy) {
        appointmentStatus = AppointmentStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
        this.cancelledBy = cancelledBy;
        touch();
    }

    public void markRescheduled() {
        appointmentStatus = AppointmentStatus.RESCHEDULED;
        touch();
    }

    public void checkIn() {
        flowStage = FlowStage.CHECK_IN;
        touch();
    }

    public void moveToWaiting() {
        flowStage = FlowStage.WAITING;
        touch();
    }

    public void startAttention() {
        flowStage = FlowStage.IN_ATTENTION;
        touch();
    }

    public void complete() {
        flowStage = FlowStage.FINISHED;
        appointmentStatus = AppointmentStatus.COMPLETED;
        touch();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getProfessionalId() {
        return professionalId;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public UUID getRescheduledFromId() {
        return rescheduledFromId;
    }

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public FlowStage getFlowStage() {
        return flowStage;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public UUID getCancelledBy() {
        return cancelledBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }
}
