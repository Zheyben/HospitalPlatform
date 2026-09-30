package com.hospital.platform.patients.contract;

import java.util.UUID;

public record PatientReference(
        UUID id,
        boolean active
) {
}
