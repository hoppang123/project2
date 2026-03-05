package com.company.asset.web.sanction;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.sanction.AssetSanction;
import com.company.asset.domain.sanction.SanctionStatus;
import com.company.asset.repository.AssetSanctionRepository;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.sanction.dto.MySanctionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sanctions")
public class SanctionController {

    private final AssetSanctionRepository sanctionRepository;

    @GetMapping("/me")
    public ApiResponse<MySanctionResponse> mySanction(@AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal.getUserId();
        LocalDateTime now = LocalDateTime.now();

        return sanctionRepository
                .findFirstByUserIdAndStatusAndEndsAtAfterOrderByEndsAtDesc(userId, SanctionStatus.ACTIVE, now)
                .map(s -> ApiResponse.ok(toDto(s)))
                .orElseGet(() -> ApiResponse.ok(new MySanctionResponse(false, null, null, null, null, 0, null)));
    }

    private MySanctionResponse toDto(AssetSanction s) {
        return new MySanctionResponse(
                true,
                s.getReason().name(),
                s.getStatus().name(),
                s.getStartsAt(),
                s.getEndsAt(),
                s.getPoints(),
                s.getMemo()
        );
    }
}