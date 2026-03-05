package com.company.asset.web.rental.dto;

import com.company.asset.domain.rental.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RentalRequestDetailResponse {
    private Long id;
    private RequestStatus status;
    private String requesterEmail;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    private Long rentalId; // 승인완료 후 생성

    private List<RentalRequestItemResponse> items;
    private List<ApprovalStepResponse> steps;
}
