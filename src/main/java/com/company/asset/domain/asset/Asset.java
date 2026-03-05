package com.company.asset.domain.asset;

import com.company.asset.domain.department.Department;
import com.company.asset.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "asset",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_asset_code", columnNames = {"asset_code"}),
                @UniqueConstraint(name = "uk_serial_no", columnNames = {"serial_no"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== 기본 정보 =====
    @Column(name = "asset_code", nullable = false, updatable = false)
    private String assetCode;

    @Column(name = "serial_no", unique = true)
    private String serialNo;

    @Column(nullable = false)
    private String name;

    private String location;

    private LocalDate purchaseDate;

    private Long price;

    // ===== 상태 =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.AVAILABLE;

    // ===== 연관관계 =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AssetCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_dept_id")
    private Department ownerDept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    // ✅ Lombok/필드명 이슈가 있어도 무조건 존재하도록 명시 getter
    public String getAssetCode() {
        return this.assetCode;
    }

    /**
     * 생성자(등록/초기데이터에서 사용)
     */
    public Asset(
            AssetCategory category,
            String assetCode,
            String serialNo,
            String name,
            String location,
            LocalDate purchaseDate,
            Long price,
            Department ownerDept,
            User manager
    ) {
        this.category = category;
        this.assetCode = assetCode;
        this.serialNo = serialNo;
        this.name = name;
        this.location = location;
        this.purchaseDate = purchaseDate;
        this.price = price;
        this.ownerDept = ownerDept;
        this.manager = manager;
        this.status = AssetStatus.AVAILABLE;
    }

    /**
     * 자산 기본 정보 수정
     * - assetCode는 정책상 변경 불가(updatable=false)라 여기서도 변경하지 않음
     */
    public void updateBasic(
            AssetCategory category,
            String serialNo,
            String name,
            String location,
            LocalDate purchaseDate,
            Long price,
            Department ownerDept,
            User manager
    ) {
        this.category = category;
        this.serialNo = serialNo;
        this.name = name;
        this.location = location;
        this.purchaseDate = purchaseDate;
        this.price = price;
        this.ownerDept = ownerDept;
        this.manager = manager;
    }

    public void changeStatus(AssetStatus to) {
        this.status = to;
    }
}