package com.company.asset.domain.maintenance;

import com.company.asset.domain.asset.Asset;
import com.company.asset.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(
        name = "asset_maintenances",
        indexes = {
                @Index(name = "idx_maint_asset_status", columnList = "asset_id,status"),
                @Index(name = "idx_maint_dates", columnList = "startDate,endDate")
        }
)
public class AssetMaintenance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.PLANNED;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 255)
    private String reason;

    public void cancel() {
        this.status = MaintenanceStatus.CANCELED;
    }

    public void done() {
        this.status = MaintenanceStatus.DONE;
    }
}