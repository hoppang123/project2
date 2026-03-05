package com.company.asset.repository;

import com.company.asset.domain.rental.Rental;
import com.company.asset.domain.rental.RentalItem;
import com.company.asset.domain.rental.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    // =========================
    // 기본 조회/카운트
    // =========================
    Page<Rental> findByRenterId(Long renterId, Pageable pageable);

    Page<Rental> findByStatus(RentalStatus status, Pageable pageable);

    long countByStatus(RentalStatus status);

    List<Rental> findByStatusInAndEndDateBeforeAndReturnedAtIsNull(List<RentalStatus> statuses, LocalDate today);

    // 자산 대여 기간 겹침 체크(대여/연체/반납요청 중 + returnedAt null)
    @Query("""
        select case when count(r) > 0 then true else false end
        from Rental r
        where r.status in ('ACTIVE','OVERDUE','RETURN_REQUESTED')
          and r.returnedAt is null
          and exists (
            select 1 from RentalItem ri
            where ri.rental = r and ri.asset.id = :assetId
          )
          and r.startDate <= :endDate
          and r.endDate >= :startDate
    """)
    boolean existsAssetRentalOverlap(Long assetId, LocalDate startDate, LocalDate endDate);

    // 사용자별 카운트(저반납률 분류 등에서 사용)
    long countByRenterId(Long renterId);

    long countByStatusAndRenterId(RentalStatus status, Long renterId);

    // =========================
    // ✅ 통계용 추가 (AdminStats)
    // =========================

    /**
     * 특정 기간 동안 "대여 시작" 건수
     * - issuedAt 기준 (대여 승인 완료 후 Rental 생성 시점)
     */
    long countByIssuedAtBetween(LocalDateTime from, LocalDateTime to);

    /**
     * 특정 기간 동안 "반납 완료" 건수
     * - returnedAt 기준 (confirmReturn 시점)
     */
    long countByReturnedAtBetween(LocalDateTime from, LocalDateTime to);

    /**
     * 일별 "대여 시작" 추이
     * - 반환: [java.sql.Date, count]
     */
    @Query("""
        select function('date', r.issuedAt) as d, count(r)
        from Rental r
        where r.issuedAt >= :from and r.issuedAt < :to
        group by function('date', r.issuedAt)
        order by d asc
    """)
    List<Object[]> countDailyIssuedAt(LocalDateTime from, LocalDateTime to);

    /**
     * 일별 "반납 완료" 추이
     * - 반환: [java.sql.Date, count]
     */
    @Query("""
        select function('date', r.returnedAt) as d, count(r)
        from Rental r
        where r.returnedAt is not null
          and r.returnedAt >= :from and r.returnedAt < :to
        group by function('date', r.returnedAt)
        order by d asc
    """)
    List<Object[]> countDailyReturnedAt(LocalDateTime from, LocalDateTime to);
}