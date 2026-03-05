package com.company.asset.web.asset;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.repository.AssetHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetHistoryController {

    private final AssetHistoryRepository historyRepository;

    @GetMapping("/{assetId}/histories")
    public ApiResponse<Page<AssetHistoryResponse>> histories(
            @PathVariable Long assetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<AssetHistoryResponse> data = historyRepository.findByAssetId(assetId, pageable)
                .map(h -> new AssetHistoryResponse(
                        h.getId(),
                        h.getAction().name(),
                        h.getBeforeStatus() != null ? h.getBeforeStatus().name() : null,
                        h.getAfterStatus() != null ? h.getAfterStatus().name() : null,
                        h.getActor() != null ? h.getActor().getEmail() : null,
                        h.getNote(),
                        h.getCreatedAt()
                ));

        return ApiResponse.ok(data);
    }

    public record AssetHistoryResponse(
            Long id,
            String action,
            String beforeStatus,
            String afterStatus,
            String actorEmail,
            String note,
            LocalDateTime createdAt
    ) {}
}