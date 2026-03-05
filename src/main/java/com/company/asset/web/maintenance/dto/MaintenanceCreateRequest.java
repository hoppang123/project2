package com.company.asset.web.maintenance.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class MaintenanceCreateRequest {

    @NotNull
    private Long assetId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String reason;

    public Long getAssetId() { return assetId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getReason() { return reason; }
}