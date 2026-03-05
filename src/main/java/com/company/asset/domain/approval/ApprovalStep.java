package com.company.asset.domain.approval;

import com.company.asset.domain.rental.RentalRequest;
import com.company.asset.domain.user.Role;
import com.company.asset.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    private RentalRequest request;

    @Column(nullable=false)
    private int stepNo; // 1,2...

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role approverRole; // MANAGER, ASSET_ADMIN ...

    @ManyToOne(fetch=FetchType.LAZY)
    private User approverUser; // 지정 승인자 (옵션)

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    private String reason;
    private LocalDateTime actedAt;

    public void approve(String note) {
        this.status = ApprovalStatus.APPROVED;
        this.reason = note;
        this.actedAt = java.time.LocalDateTime.now();
    }
    public void reject(String reason) {
        this.status = ApprovalStatus.REJECTED;
        this.reason = reason;
        this.actedAt = java.time.LocalDateTime.now();
    }

    public ApprovalStep(RentalRequest request, int stepNo, Role approverRole) {
        this.request = request;
        this.stepNo = stepNo;
        this.approverRole = approverRole;
        this.status = ApprovalStatus.PENDING;
    }

}
