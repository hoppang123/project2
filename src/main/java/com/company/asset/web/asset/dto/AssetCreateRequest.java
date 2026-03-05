package com.company.asset.web.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AssetCreateRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String assetCode;

    private String serialNo;

    @NotBlank
    private String name;

    private String location;

    private LocalDate purchaseDate;

    private Long price;

    private Long ownerDeptId;     // optional
    private Long managerUserId;   // optional
}