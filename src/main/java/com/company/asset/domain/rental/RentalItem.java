package com.company.asset.domain.rental;

import com.company.asset.domain.asset.Asset;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.RENTED;

    public RentalItem(Rental rental, Asset asset) {
        this.rental = rental;
        this.asset = asset;
    }

    public void markReturned() { this.status = ItemStatus.RETURNED; }
}
