package com.hospital.platform.patients.entity;

import com.hospital.platform.patients.domain.PatientDemographics;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patients", uniqueConstraints = @UniqueConstraint(
        name = "uq_patients_document_identity", columnNames = {"document_type", "document_number"}))
public class Patient {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 50)
    private String phone;

    @Column(length = 150)
    private String insurance;

    @Column(name = "insurance_id")
    private UUID insuranceId;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String sex;

    @Column(name = "marital_status", length = 80)
    private String maritalStatus;

    @Column(length = 120)
    private String occupation;

    @Column(length = 120)
    private String district;

    @Column(name = "education_level", length = 120)
    private String educationLevel;

    @Column(name = "affiliation_number", length = 60)
    private String affiliationNumber;

    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship", length = 80)
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_phone", length = 50)
    private String emergencyContactPhone;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Patient() {
    }

    public Patient(
            UUID id,
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            String phone,
            String address
    ) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
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

    public void updateAdministrativeInfo(
            String documentType,
            String documentNumber,
            LocalDate birthDate,
            String phone,
            String address
    ) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.birthDate = birthDate;
        this.phone = phone;
        this.address = address;
        touch();
    }

    public void deactivate() {
        deletedAt = LocalDateTime.now();
        touch();
    }

    public void linkUser(UUID userId) {
        this.userId = userId;
        touch();
    }

    public void setInsurance(UUID insuranceId, String insurance) {
        this.insuranceId = insuranceId;
        this.insurance = insurance;
        touch();
    }

    public void setSex(String sex) {
        this.sex = sex;
        touch();
    }

    public void setDemographics(PatientDemographics demographics) {
        maritalStatus = demographics.maritalStatus();
        occupation = demographics.occupation();
        district = demographics.district();
        educationLevel = demographics.educationLevel();
        affiliationNumber = demographics.affiliationNumber();
        emergencyContactName = demographics.emergencyContactName();
        emergencyContactRelationship = demographics.emergencyContactRelationship();
        emergencyContactPhone = demographics.emergencyContactPhone();
        touch();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPhone() {
        return phone;
    }

    public String getInsurance() {
        return insurance;
    }

    public UUID getInsuranceId() {
        return insuranceId;
    }

    public String getAddress() {
        return address;
    }

    public String getSex() {
        return sex;
    }

    public PatientDemographics getDemographics() {
        return new PatientDemographics(maritalStatus, occupation, district, educationLevel,
                affiliationNumber, emergencyContactName, emergencyContactRelationship, emergencyContactPhone);
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }
}
