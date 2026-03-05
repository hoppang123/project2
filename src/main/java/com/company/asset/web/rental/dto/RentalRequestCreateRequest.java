package com.company.asset.web.rental.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class RentalRequestCreateRequest {

    @NotEmpty
    private List<Long> assetIds;

    @NotBlank
    private String purpose;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
