package com.company.asset.web.reservation.dto;

import com.company.asset.domain.reservation.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationSummaryResponse {
    private Long id;
    private Long assetId;
    private String assetCode;
    private String assetName;

    private LocalDate startDate;
    private LocalDate endDate;

    private ReservationStatus status;
    private LocalDateTime createdAt;
}