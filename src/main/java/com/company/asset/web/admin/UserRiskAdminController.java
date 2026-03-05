package com.company.asset.web.admin;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.analytics.UserAnalyticsService;
import com.company.asset.domain.analytics.UserRiskLevel;
import com.company.asset.repository.UserRiskProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/risk")
public class UserRiskAdminController {

    private final UserRiskProfileRepository riskProfileRepository;
    private final UserAnalyticsService analyticsService;

    @GetMapping("/low-return")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> lowReturnUsers() {
        return ApiResponse.ok(riskProfileRepository.findByLevel(UserRiskLevel.LOW_RETURN));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<?> userRisk(@PathVariable Long userId) {
        return ApiResponse.ok(riskProfileRepository.findById(userId).orElse(null));
    }

    /**
     * ✅ 강제 재계산 (테스트/운영용)
     * POST /api/admin/risk/recalc?threshold=0.7&minCount=3
     */
    @PostMapping("/recalc")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> recalc(
            @RequestParam(defaultValue = "0.7") double threshold,
            @RequestParam(defaultValue = "3") long minCount
    ) {
        analyticsService.recalcAll(threshold, minCount);
        return ApiResponse.ok();
    }
}