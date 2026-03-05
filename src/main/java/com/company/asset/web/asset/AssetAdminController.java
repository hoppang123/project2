package com.company.asset.web.asset;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.asset.AssetAdminService;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.asset.dto.AssetStatusChangeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetAdminController {

    private final AssetAdminService assetAdminService;

    @PatchMapping("/{assetId}/status")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> changeStatus(
            @PathVariable Long assetId,
            @Valid @RequestBody AssetStatusChangeRequest req,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        assetAdminService.changeStatus(assetId, req.getStatus(), principal.getUserId(), req.getNote());
        return ApiResponse.ok();
    }
}