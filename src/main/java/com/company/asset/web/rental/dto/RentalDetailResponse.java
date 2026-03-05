package com.company.asset.web.rental.dto;

import com.company.asset.domain.rental.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RentalDetailResponse {
    private Long id;
    private RentalStatus status;
    private String renterEmail;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime issuedAt;
    private LocalDateTime returnedAt;

    private List<RentalItemResponse> items;
}
