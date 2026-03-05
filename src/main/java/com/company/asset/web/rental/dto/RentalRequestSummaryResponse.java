package com.company.asset.web.rental.dto;

import com.company.asset.domain.rental.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RentalRequestSummaryResponse {
    private Long id;
    private RequestStatus status;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private int itemCount;
    private LocalDateTime createdAt;
}
