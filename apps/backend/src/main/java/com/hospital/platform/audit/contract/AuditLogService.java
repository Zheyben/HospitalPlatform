package com.hospital.platform.audit.contract;

import java.util.Map;
import java.util.UUID;

public interface AuditLogService {

    void record(
            AuditEventType eventType,
            String entityName,
            UUID entityId,
            Map<String, Object> oldValues,
            Map<String, Object> newValues
    );
}
