package com.company.asset.web.maintenance;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.maintenance.MaintenanceService;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.maintenance.dto.MaintenanceCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/maintenances")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Long> create(@Valid @RequestBody MaintenanceCreateRequest req,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        Long id = maintenanceService.create(
                principal.getUserId(),
                req.getAssetId(),
                req.getStartDate(),
                req.getEndDate(),
                req.getReason()
        );
        return ApiResponse.ok(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        maintenanceService.cancel(id, principal.getUserId());
        return ApiResponse.ok();
    }
}