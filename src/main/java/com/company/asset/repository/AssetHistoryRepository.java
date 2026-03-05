package com.company.asset.repository;

import com.company.asset.domain.asset.AssetHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {

    // ✅ 자산별 이력 최신순 페이징 조회 (AssetHistoryController에서 사용)
    Page<AssetHistory> findByAssetId(Long assetId, Pageable pageable);

    // ✅ 자산별 이력 최신 N개 (대시보드/상세 상단 요약용)
    List<AssetHistory> findTop10ByAssetIdOrderByIdDesc(Long assetId);
}