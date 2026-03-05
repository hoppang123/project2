package com.company.asset.repository;

import com.company.asset.domain.asset.Asset;
import com.company.asset.domain.asset.AssetStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // 상태만
    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    // keyword만 (assetCode/name/serialNo/location)
    @Query("""
        select a from Asset a
        where
            lower(a.assetCode) like lower(concat('%', :keyword, '%'))
            or lower(a.name) like lower(concat('%', :keyword, '%'))
            or (a.serialNo is not null and lower(a.serialNo) like lower(concat('%', :keyword, '%')))
            or (a.location is not null and lower(a.location) like lower(concat('%', :keyword, '%')))
        """)
    Page<Asset> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // status + keyword
    @Query("""
        select a from Asset a
        where a.status = :status
          and (
                lower(a.assetCode) like lower(concat('%', :keyword, '%'))
             or lower(a.name) like lower(concat('%', :keyword, '%'))
             or (a.serialNo is not null and lower(a.serialNo) like lower(concat('%', :keyword, '%')))
             or (a.location is not null and lower(a.location) like lower(concat('%', :keyword, '%')))
          )
        """)
    Page<Asset> findByStatusAndKeyword(
            @Param("status") AssetStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // (대시보드용) 상태 카운트
    long countByStatus(AssetStatus status);

    // (선택) 코드 중복 체크를 하고 싶으면
    boolean existsByAssetCode(String assetCode);
}