package com.company.asset.repository;

import com.company.asset.domain.maintenance.AssetMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, Long> {

    // =========================
    // 기존: 정비 기간 겹침 체크
    // =========================
    @Query("""
        select case when count(m) > 0 then true else false end
        from AssetMaintenance m
        where m.asset.id = :assetId
          and m.status = 'PLANNED'
          and m.startDate <= :endDate
          and m.endDate >= :startDate
    """)
    boolean existsOverlap(Long assetId, LocalDate startDate, LocalDate endDate);

    // =========================
    // ✅ 통계용 추가
    // =========================

    /**
     * 특정 기간 동안 "정비 생성" 건수 (createdAt 기준)
     */
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    /**
     * 일별 "정비 생성" 추이 (createdAt 기준)
     * - 반환: [java.sql.Date, count]
     */
    @Query("""
        select function('date', m.createdAt) as d, count(m)
        from AssetMaintenance m
        where m.createdAt >= :from and m.createdAt < :to
        group by function('date', m.createdAt)
        order by d asc
    """)
    List<Object[]> countDailyCreatedAt(LocalDateTime from, LocalDateTime to);
}