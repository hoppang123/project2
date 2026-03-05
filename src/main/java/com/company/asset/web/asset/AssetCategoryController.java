package com.company.asset.web.asset;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.asset.AssetCategory;
import com.company.asset.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/asset-categories")
public class AssetCategoryController {

    private final AssetCategoryRepository assetCategoryRepository;

    @GetMapping
    public ApiResponse<List<AssetCategoryResponse>> list() {
        List<AssetCategoryResponse> data = assetCategoryRepository.findAll().stream()
                .map(c -> new AssetCategoryResponse(c.getId(), c.getName()))
                .toList();
        return ApiResponse.ok(data);
    }

    public record AssetCategoryResponse(Long id, String name) {}
}