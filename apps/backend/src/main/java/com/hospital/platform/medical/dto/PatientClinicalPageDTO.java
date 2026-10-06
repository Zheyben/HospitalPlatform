package com.hospital.platform.medical.dto;

import java.util.List;

public record PatientClinicalPageDTO<T>(List<T> items, int limit, int offset, boolean hasMore) {
}
