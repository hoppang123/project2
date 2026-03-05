package com.company.asset.web.rental.dto;

import com.company.asset.domain.approval.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApprovalStepResponse {
    private int stepNo;
    private String approverRole;      // MANAGER / ASSET_ADMIN
    private ApprovalStatus status;    // PENDING / APPROVED / REJECTED
    private String reason;
    private LocalDateTime actedAt;
}
