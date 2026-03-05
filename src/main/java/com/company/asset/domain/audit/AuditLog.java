package com.company.asset.domain.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_actor", columnList = "actorUserId,createdAt"),
                @Index(name = "idx_audit_target", columnList = "targetType,targetId,createdAt")
        }
)
public class AuditLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AuditAction action;

    @Column(nullable = false, length = 60)
    private String targetType;   // e.g. "RentalRequest", "Rental", "Asset", "Reservation", "Maintenance"

    @Column(nullable = false)
    private Long targetId;

    @Lob
    private String beforeJson;

    @Lob
    private String afterJson;

    @Column(length = 255)
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}