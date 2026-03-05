package com.company.asset.domain.asset;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.domain.department.Department;
import com.company.asset.domain.user.User;
import com.company.asset.repository.*;
import com.company.asset.web.asset.dto.AssetCreateRequest;
import com.company.asset.web.asset.dto.AssetResponse;
import com.company.asset.web.asset.dto.AssetUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AssetHistoryRepository assetHistoryRepository;

    /**
     * 자산 목록 조회(검색/필터)
     */
    @Transactional(readOnly = true)
    public Page<AssetResponse> search(String keyword, AssetStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        Page<Asset> result;

        if (status != null && hasKeyword) {
            result = assetRepository.findByStatusAndKeyword(status, keyword.trim(), pageable);
        } else if (status != null) {
            result = assetRepository.findByStatus(status, pageable);
        } else if (hasKeyword) {
            result = assetRepository.findByKeyword(keyword.trim(), pageable);
        } else {
            result = assetRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    /**
     * 자산 단건 조회
     */
    @Transactional(readOnly = true)
    public AssetResponse get(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return toResponse(asset);
    }

    /**
     * 자산 등록 + 이력(CREATE)
     */
    @Transactional
    public Long create(AssetCreateRequest req, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        AssetCategory category = assetCategoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Department ownerDept = null;
        if (req.getOwnerDeptId() != null) {
            ownerDept = departmentRepository.findById(req.getOwnerDeptId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        }

        User manager = null;
        if (req.getManagerUserId() != null) {
            manager = userRepository.findById(req.getManagerUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        }

        Asset asset = new Asset(
                category,
                req.getAssetCode().trim(),
                (req.getSerialNo() != null && !req.getSerialNo().isBlank()) ? req.getSerialNo().trim() : null,
                req.getName().trim(),
                (req.getLocation() != null && !req.getLocation().isBlank()) ? req.getLocation().trim() : null,
                req.getPurchaseDate(),
                req.getPrice(),
                ownerDept,
                manager
        );

        assetRepository.save(asset);

        assetHistoryRepository.save(AssetHistory.builder()
                .asset(asset)
                .action(AssetAction.CREATE)
                .beforeStatus(null)
                .afterStatus(asset.getStatus())
                .actor(actor)
                .note("자산 등록")
                .build());

        return asset.getId();
    }

    /**
     * 자산 수정 + 이력(UPDATE)
     */
    @Transactional
    public void update(Long assetId, AssetUpdateRequest req, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        AssetCategory category = assetCategoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Department ownerDept = null;
        if (req.getOwnerDeptId() != null) {
            ownerDept = departmentRepository.findById(req.getOwnerDeptId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        }

        User manager = null;
        if (req.getManagerUserId() != null) {
            manager = userRepository.findById(req.getManagerUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        }

        AssetStatus beforeStatus = asset.getStatus();

        asset.updateBasic(
                category,
                (req.getSerialNo() != null && !req.getSerialNo().isBlank()) ? req.getSerialNo().trim() : null,
                req.getName().trim(),
                (req.getLocation() != null && !req.getLocation().isBlank()) ? req.getLocation().trim() : null,
                req.getPurchaseDate(),
                req.getPrice(),
                ownerDept,
                manager
        );

        assetHistoryRepository.save(AssetHistory.builder()
                .asset(asset)
                .action(AssetAction.UPDATE)
                .beforeStatus(beforeStatus)
                .afterStatus(asset.getStatus())
                .actor(actor)
                .note("자산 정보 수정")
                .build());
    }

    /**
     * 자산 삭제(물리 삭제)
     * - 운영에서는 DISPOSED로 상태 변경 권장
     */
    @Transactional
    public void delete(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        assetRepository.delete(asset);
    }

    // =========================
    // mapper
    // =========================
    private AssetResponse toResponse(Asset a) {
        return new AssetResponse(
                a.getId(),
                a.getAssetCode(),
                a.getSerialNo(),
                a.getName(),
                a.getStatus(),
                a.getLocation(),
                a.getPurchaseDate(),
                a.getPrice(),
                a.getCategory() != null ? a.getCategory().getId() : null,
                a.getCategory() != null ? a.getCategory().getName() : null,
                a.getOwnerDept() != null ? a.getOwnerDept().getId() : null,
                a.getOwnerDept() != null ? a.getOwnerDept().getName() : null,
                a.getManager() != null ? a.getManager().getId() : null,
                a.getManager() != null ? a.getManager().getEmail() : null
        );
    }

}