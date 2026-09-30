package com.hospital.platform.audit.contract;

import com.hospital.platform.audit.entity.AuditLog;
import com.hospital.platform.audit.repository.AuditLogRepository;
import com.hospital.platform.users.service.CurrentUserService;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Lazy
class DatabaseAuditLogService implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    DatabaseAuditLogService(
            AuditLogRepository auditLogRepository,
            CurrentUserService currentUserService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            AuditEventType eventType,
            String entityName,
            UUID entityId,
            Map<String, Object> oldValues,
            Map<String, Object> newValues
    ) {
        UUID actorUserId = currentUserService.currentUserId();
        auditLogRepository.save(new AuditLog(
                actorUserId,
                eventType,
                entityName,
                entityId,
                oldValues,
                newValues
        ));
    }
}
