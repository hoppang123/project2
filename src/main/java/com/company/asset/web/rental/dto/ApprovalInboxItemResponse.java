package com.company.asset.web.rental.dto;

import com.company.asset.domain.rental.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApprovalInboxItemResponse {
    private Long requestId;
    private RequestStatus status;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private int itemCount;
    private LocalDateTime createdAt;

    private int stepNo;              // ✅ 내가 처리할 단계
    private String approverRole;     // ✅ 내가 처리할 역할 (MANAGER/ASSET_ADMIN)
}