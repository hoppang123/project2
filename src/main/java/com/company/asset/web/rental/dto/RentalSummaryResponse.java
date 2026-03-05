package com.company.asset.web.rental.dto;

import com.company.asset.domain.rental.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RentalSummaryResponse {
    private Long id;
    private RentalStatus status;
    private String renterEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime issuedAt;
    private LocalDateTime returnedAt;
    private int itemCount;
}
