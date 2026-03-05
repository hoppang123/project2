package com.company.asset.web.audit;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.repository.AuditLogRepository;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.audit.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/me")
    public ApiResponse<Page<AuditLogResponse>> myAudits(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> result = auditLogRepository
                .findByActorUserIdOrderByCreatedAtDesc(principal.getUserId(), pageable)
                .map(AuditLogResponse::from);

        return ApiResponse.ok(result);
    }

    @GetMapping("/target")
    public ApiResponse<Page<AuditLogResponse>> targetAudits(
            @RequestParam String type,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> result = auditLogRepository
                .findByTargetTypeAndTargetIdOrderByCreatedAtDesc(type, targetId, pageable)
                .map(AuditLogResponse::from);

        return ApiResponse.ok(result);
    }

    @GetMapping("/actor/{userId}")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<AuditLogResponse>> actorAudits(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> result = auditLogRepository
                .findByActorUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(AuditLogResponse::from);

        return ApiResponse.ok(result);
    }
}