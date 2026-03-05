package com.company.asset.repository;

import com.company.asset.domain.reservation.AssetReservation;
import com.company.asset.domain.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssetReservationRepository extends JpaRepository<AssetReservation, Long> {

    // 자산 + 상태로 예약 목록
    List<AssetReservation> findByAssetIdAndStatus(Long assetId, ReservationStatus status);

    // 내 예약 목록(페이지)
    Page<AssetReservation> findByReserverId(Long reserverId, Pageable pageable);

    // 예약 겹침 체크
    @Query("""
        select case when count(r) > 0 then true else false end
        from AssetReservation r
        where r.asset.id = :assetId
          and r.status = 'RESERVED'
          and r.startDate <= :endDate
          and r.endDate >= :startDate
    """)
    boolean existsOverlap(Long assetId, LocalDate startDate, LocalDate endDate);

    // 예약자 본인 여부 체크/조회용(취소/체크아웃에서 유용)
    Optional<AssetReservation> findByIdAndReserverId(Long id, Long reserverId);

    // 특정 사용자의 RESERVED 예약 존재 여부(필요 시)
    boolean existsByReserverIdAndStatus(Long reserverId, ReservationStatus status);

    // =========================
    // ✅ 통계용 추가
    // =========================

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    // 반환: [java.sql.Date, count]
    @Query("""
        select function('date', r.createdAt) as d, count(r)
        from AssetReservation r
        where r.createdAt >= :from and r.createdAt < :to
        group by function('date', r.createdAt)
        order by d asc
    """)
    List<Object[]> countDailyCreatedAt(LocalDateTime from, LocalDateTime to);
}