package com.hospital.platform.patients.repository;

import java.util.UUID;

public interface ReceptionPatientSearchRow {
    UUID getPatientId();
    String getDocumentType();
    String getDocumentNumber();
    String getPatientName();
}
