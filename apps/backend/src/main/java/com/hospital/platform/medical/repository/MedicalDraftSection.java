package com.hospital.platform.medical.repository;

public enum MedicalDraftSection {
    HISTORY("history"),
    ASSESSMENT("assessment"),
    PRESCRIPTION("prescription");

    private final String key;

    MedicalDraftSection(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
