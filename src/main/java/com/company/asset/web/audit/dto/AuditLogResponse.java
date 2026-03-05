package com.company.asset.web.audit.dto;

import com.company.asset.domain.audit.AuditAction;
import com.company.asset.domain.audit.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private Long actorUserId;
    private AuditAction action;
    private String targetType;
    private Long targetId;
    private String note;
    private LocalDateTime createdAt;

    // 필요하면 before/after도 내려줄 수 있는데(용량 큼) 일단은 제외
    // private String beforeJson;
    // private String afterJson;

    public static AuditLogResponse from(AuditLog a) {
        AuditLogResponse r = new AuditLogResponse();
        r.id = a.getId();
        r.actorUserId = a.getActorUserId();
        r.action = a.getAction();
        r.targetType = a.getTargetType();
        r.targetId = a.getTargetId();
        r.note = a.getNote();
        r.createdAt = a.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getActorUserId() { return actorUserId; }
    public AuditAction getAction() { return action; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getNote() { return note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}