package com.company.asset.web.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AssetUpdateRequest {

    @NotNull
    private Long categoryId;

    // Asset.java에서 assetCode는 updatable=false라 "변경 불가" 정책으로 잡았음
    // 그래서 UpdateRequest엔 assetCode를 안 넣는 게 깔끔함.
    // 변경 가능하게 하고 싶으면 Asset.java에서 updatable=false 제거하고 여기에 추가해.

    private String serialNo;

    @NotBlank
    private String name;

    private String location;

    private LocalDate purchaseDate;

    private Long price;

    private Long ownerDeptId;     // optional
    private Long managerUserId;   // optional
}