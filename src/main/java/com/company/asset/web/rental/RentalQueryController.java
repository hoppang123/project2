package com.company.asset.web.rental;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.rental.*;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.rental.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.company.asset.domain.user.Role;
import com.company.asset.web.rental.dto.ApprovalInboxItemResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
public class RentalQueryController {

    private final RentalQueryService queryService;

    // 내 대여 신청 목록
    @GetMapping("/requests/mine")
    public ApiResponse<Page<RentalRequestSummaryResponse>> myRequests(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(queryService.myRequests(principal.getUserId(), page, size));
    }

    // 대여 신청 상세(아이템/승인 단계 포함)
    @GetMapping("/requests/{id}")
    public ApiResponse<RentalRequestDetailResponse> requestDetail(@PathVariable Long id) {
        return ApiResponse.ok(queryService.requestDetail(id));
    }

    // 승인자 대기함(일단 APPROVING 기준) - MANAGER/ASSET_ADMIN 이상만
    @GetMapping("/requests/pending")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<RentalRequestSummaryResponse>> pending(
            @RequestParam(defaultValue = "APPROVING") RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(queryService.pendingBox(status, page, size));
    }

    // 내 대여 목록(대여중/반납요청/반납완료 포함)
    @GetMapping("/mine")
    public ApiResponse<Page<RentalSummaryResponse>> myRentals(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(queryService.myRentals(principal.getUserId(), page, size));
    }

    // 대여 상세(아이템 포함)
    @GetMapping("/{rentalId}")
    public ApiResponse<RentalDetailResponse> rentalDetail(@PathVariable Long rentalId) {
        return ApiResponse.ok(queryService.rentalDetail(rentalId));
    }

    @GetMapping("/requests/inbox")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<RentalRequestSummaryResponse>> inbox(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // ✅ SUPER_ADMIN은 일단 ASSET_ADMIN처럼 처리(원하면 둘 다 보이게 확장 가능)
        Role myRole = Role.valueOf(principal.getRoleName());
        if (myRole == Role.SUPER_ADMIN) myRole = Role.ASSET_ADMIN;

        return ApiResponse.ok(queryService.myApprovalInbox(myRole, page, size));
    }

    @GetMapping
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<RentalSummaryResponse>> allRentals(
            @RequestParam(required = false) RentalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(queryService.rentals(status, page, size));
    }

    @GetMapping("/requests/inbox-v2")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<ApprovalInboxItemResponse>> inboxV2(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Role myRole = Role.valueOf(principal.getRoleName());
        if (myRole == Role.SUPER_ADMIN) myRole = Role.ASSET_ADMIN;

        return ApiResponse.ok(queryService.myApprovalInboxV2(myRole, page, size));
    }
}
