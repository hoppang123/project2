package com.company.asset.web.stats;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.stats.AdminStatsService;
import com.company.asset.web.stats.dto.AdminStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<AdminStatsResponse> stats(@RequestParam(defaultValue = "14") int days) {
        return ApiResponse.ok(adminStatsService.getStats(days));
    }
}