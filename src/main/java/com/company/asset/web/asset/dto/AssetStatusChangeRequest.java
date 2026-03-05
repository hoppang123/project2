package com.company.asset.web.asset.dto;

import com.company.asset.domain.asset.AssetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AssetStatusChangeRequest {
    @NotNull
    private AssetStatus status;

    private String note;
}
