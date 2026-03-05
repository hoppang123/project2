package com.company.asset.web.reservation.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ReservationCreateRequest {

    @NotNull
    private Long assetId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String note;

    public Long getAssetId() { return assetId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getNote() { return note; }
}