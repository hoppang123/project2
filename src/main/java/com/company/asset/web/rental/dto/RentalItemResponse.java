package com.company.asset.web.rental.dto;

import com.company.asset.domain.rental.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RentalItemResponse {
    private Long assetId;
    private String assetCode;
    private String name;
    private ItemStatus status;
}
