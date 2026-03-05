package com.company.asset.domain.rental;

import com.company.asset.domain.asset.Asset;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalRequestItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private RentalRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Asset asset;

    public RentalRequestItem(RentalRequest request, Asset asset) {
        this.request = request;
        this.asset = asset;
    }
}
