package com.company.asset.repository;

import com.company.asset.domain.sanction.AssetSanction;
import com.company.asset.domain.sanction.SanctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssetSanctionRepository extends JpaRepository<AssetSanction, Long> {

    // =========================
    // 기존(유지)
    // =========================
    boolean existsByUserIdAndStatusAndEndsAtAfter(Long userId, SanctionStatus status, LocalDateTime now);

    List<AssetSanction> findByStatusAndEndsAtBefore(SanctionStatus status, LocalDateTime now);

    List<AssetSanction> findByUserIdOrderByCreatedAtDesc(Long userId);

    // =========================
    // ✅ 추가: "현재 활성 제재 1건" 조회 (내 제재상태 페이지/차단 메시지에 유용)
    // =========================
    Optional<AssetSanction> findFirstByUserIdAndStatusAndEndsAtAfterOrderByEndsAtDesc(
            Long userId,
            SanctionStatus status,
            LocalDateTime now
    );

    // =========================
    // ✅ 추가: 관리자용(선택) - 제재 목록 조회/필터
    // =========================
    Page<AssetSanction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AssetSanction> findByStatusOrderByCreatedAtDesc(SanctionStatus status, Pageable pageable);

    Page<AssetSanction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // =========================
    // ✅ 추가: 운영 편의(선택) - 특정 사용자 ACTIVE 제재 전체 조회
    // =========================
    List<AssetSanction> findByUserIdAndStatusOrderByEndsAtDesc(Long userId, SanctionStatus status);
}