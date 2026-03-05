package com.company.asset.web.rental.dto;

import com.company.asset.domain.asset.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RentalRequestItemResponse {
    private Long assetId;
    private String assetCode;
    private String name;
    private AssetStatus assetStatus;
}
