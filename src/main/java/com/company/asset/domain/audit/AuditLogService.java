package com.company.asset.domain.audit;

import com.company.asset.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void log(Long actorUserId,
                    AuditAction action,
                    String targetType,
                    Long targetId,
                    Object beforeObj,
                    Object afterObj,
                    String note) {

        AuditLog audit = AuditLog.builder()
                .actorUserId(actorUserId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .beforeJson(toJsonSafe(beforeObj))
                .afterJson(toJsonSafe(afterObj))
                .note(note)
                .build();

        auditLogRepository.save(audit);
    }

    private String toJsonSafe(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // JSON 실패해도 감사로그가 전체 트랜잭션을 깨면 안 됨
            return String.valueOf(obj);
        }
    }
}