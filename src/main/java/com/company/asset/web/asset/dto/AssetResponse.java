package com.company.asset.web.asset.dto;

import com.company.asset.domain.asset.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AssetResponse {

    private Long id;
    private String assetCode;
    private String serialNo;
    private String name;
    private AssetStatus status;
    private String location;
    private LocalDate purchaseDate;
    private Long price;

    private Long categoryId;
    private String categoryName;

    private Long ownerDeptId;
    private String ownerDeptName;

    private Long managerUserId;
    private String managerEmail;
}