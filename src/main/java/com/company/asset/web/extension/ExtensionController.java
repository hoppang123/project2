package com.company.asset.web.extension;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.rental.ExtensionQueryService;
import com.company.asset.domain.rental.ExtensionWorkflowService;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.extension.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/extensions")
public class ExtensionController {

    private final ExtensionWorkflowService workflowService;
    private final ExtensionQueryService queryService;

    // 연장 요청(대여자 본인)
    @PostMapping
    public ApiResponse<Long> request(@Valid @RequestBody ExtensionCreateRequest req,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(workflowService.requestExtension(req, principal.getUserId()));
    }

    // 내 연장요청 목록
    @GetMapping("/mine")
    public ApiResponse<Page<ExtensionResponse>> mine(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(queryService.myRequests(principal.getUserId(), page, size));
    }

    // 승인 대기 목록(관리자)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<ExtensionResponse>> pending(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(queryService.pending(page, size));
    }

    // 승인(관리자)
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> approve(@PathVariable Long id,
                                     @RequestBody ExtensionApproveRequest req,
                                     @AuthenticationPrincipal CustomUserDetails principal) {
        workflowService.approve(id, principal.getUserId(), req != null ? req.getAdminNote() : null);
        return ApiResponse.ok();
    }

    // 반려(관리자)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> reject(@PathVariable Long id,
                                    @Valid @RequestBody ExtensionRejectRequest req,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        workflowService.reject(id, principal.getUserId(), req.getAdminNote());
        return ApiResponse.ok();
    }

    // 취소(요청자)
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        workflowService.cancel(id, principal.getUserId());
        return ApiResponse.ok();
    }
}
