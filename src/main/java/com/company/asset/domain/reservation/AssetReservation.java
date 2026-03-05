package com.company.asset.domain.reservation;

import com.company.asset.domain.asset.Asset;
import com.company.asset.domain.rental.Rental;
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
        name = "asset_reservations",
        indexes = {
                @Index(name = "idx_reservation_asset_status", columnList = "asset_id,status"),
                @Index(name = "idx_reservation_dates", columnList = "startDate,endDate")
        }
)
public class AssetReservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserver_id")
    private User reserver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.RESERVED;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

    @OneToOne(fetch = FetchType.LAZY)
    private Rental rental; // 체크아웃 시 생성된 대여 연결

    public void checkOut(Rental rental) {
        this.status = ReservationStatus.CHECKED_OUT;
        this.rental = rental;
    }
}