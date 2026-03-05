package com.company.asset.web.extension.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ExtensionCreateRequest {

    @NotNull
    private Long rentalId;

    @NotNull
    private LocalDate requestedEndDate;

    @NotBlank
    private String reason;
}
