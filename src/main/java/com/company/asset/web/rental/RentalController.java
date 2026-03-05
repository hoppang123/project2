package com.company.asset.web.rental;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.rental.RentalWorkflowService;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.rental.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalWorkflowService service;

    // 대여 신청(EMPLOYEE 이상이면 누구나 가능)
    @PostMapping("/requests")
    public ApiResponse<Long> createRequest(@Valid @RequestBody RentalRequestCreateRequest req,
                                           @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(service.createRequest(req, principal.getUserId()));
    }

    // 승인(MANAGER 또는 ASSET_ADMIN 단계별로)
    @PostMapping("/requests/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> approve(@PathVariable Long id,
                                     @RequestBody ApproveRequest req,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        service.approve(id, principal.getUserId(), req != null ? req.getNote() : null);
        return ApiResponse.ok();
    }

    // 반려
    @PostMapping("/requests/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> reject(@PathVariable Long id,
                                    @Valid @RequestBody RejectRequest req,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        service.reject(id, principal.getUserId(), req.getReason());
        return ApiResponse.ok();
    }

    // 반납 요청(본인만)
    @PostMapping("/{rentalId}/return-request")
    public ApiResponse<Void> requestReturn(@PathVariable Long rentalId,
                                           @AuthenticationPrincipal CustomUserDetails principal) {
        service.requestReturn(rentalId, principal.getUserId());
        return ApiResponse.ok();
    }

    // 반납 확인(자산관리자)
    @PostMapping("/{rentalId}/return-confirm")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> confirmReturn(@PathVariable Long rentalId,
                                           @AuthenticationPrincipal CustomUserDetails principal) {
        service.confirmReturn(rentalId, principal.getUserId());
        return ApiResponse.ok();
    }

    @PostMapping("/requests/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        service.cancelRequest(id, principal.getUserId());
        return ApiResponse.ok();
    }

}
