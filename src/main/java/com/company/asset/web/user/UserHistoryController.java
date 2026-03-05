package com.company.asset.web.user;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.rental.RentalQueryService;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.rental.dto.RentalSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserHistoryController {

    private final RentalQueryService rentalQueryService;

    // 내 대여/반납 기록
    @GetMapping("/me/rentals")
    public ApiResponse<Page<RentalSummaryResponse>> myRentals(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(rentalQueryService.myRentals(principal.getUserId(), page, size));
    }

    // 관리자: 특정 사용자 기록 조회
    @GetMapping("/{userId}/rentals")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<RentalSummaryResponse>> userRentals(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(rentalQueryService.userRentals(userId, page, size));
    }
}