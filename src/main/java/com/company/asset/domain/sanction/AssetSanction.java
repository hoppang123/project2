package com.company.asset.domain.sanction;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "asset_sanctions",
        indexes = {
                @Index(name = "idx_sanction_user_status", columnList = "userId,status"),
                @Index(name = "idx_sanction_ends_at", columnList = "endsAt")
        }
)
public class AssetSanction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 제재 대상 사용자 ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 제재 사유
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SanctionReason reason;

    /**
     * 제재 상태 (ACTIVE/EXPIRED/REVOKED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SanctionStatus status = SanctionStatus.ACTIVE;

    /**
     * 제재 시작/종료
     */
    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    /**
     * 누적 점수(연체일 등)
     */
    @Column(nullable = false)
    @Builder.Default
    private int points = 0;

    /**
     * 운영 메모
     */
    @Column(length = 255)
    private String memo;

    /**
     * 생성 정보
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 생성한 관리자(또는 시스템) 사용자 ID
     */
    private Long createdBy;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = SanctionStatus.ACTIVE;
    }

    public boolean isActive(LocalDateTime now) {
        return status == SanctionStatus.ACTIVE && now.isBefore(endsAt);
    }

    public void expireIfEnded(LocalDateTime now) {
        if (status == SanctionStatus.ACTIVE && !now.isBefore(endsAt)) {
            this.status = SanctionStatus.EXPIRED;
        }
    }

    public void revoke(String revokeMemo) {
        this.status = SanctionStatus.REVOKED;
        if (revokeMemo != null && !revokeMemo.isBlank()) {
            this.memo = (this.memo == null || this.memo.isBlank())
                    ? revokeMemo
                    : (this.memo + " | REVOKE: " + revokeMemo);
        }
    }

    public void expire() {
        if (this.status == SanctionStatus.ACTIVE) {
            this.status = SanctionStatus.EXPIRED;
        }
    }
}