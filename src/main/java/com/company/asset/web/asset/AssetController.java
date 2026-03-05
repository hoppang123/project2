package com.company.asset.web.asset;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.asset.AssetService;
import com.company.asset.domain.asset.AssetStatus;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.asset.dto.AssetCreateRequest;
import com.company.asset.web.asset.dto.AssetResponse;
import com.company.asset.web.asset.dto.AssetUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    // 목록 조회 (keyword/status/page/size)
    @GetMapping
    public ApiResponse<Page<AssetResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(assetService.search(keyword, status, page, size));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<AssetResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(assetService.get(id));
    }

    // 등록 (관리자)
    @PostMapping
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Long> create(
            @Valid @RequestBody AssetCreateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        return ApiResponse.ok(assetService.create(req, principal.getUserId()));
    }

    // 수정 (관리자)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest req,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        assetService.update(id, req, principal.getUserId());
        return ApiResponse.ok();
    }

    // 삭제 (관리자) - 운영은 보통 상태 변경 권장
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ASSET_ADMIN') or hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ApiResponse.ok();
    }
}