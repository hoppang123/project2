package com.company.asset.domain.policy;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalPolicy {

    @Id
    private Long id = 1L; // 단일 row 정책

    // 최대 대여 일수
    @Column(nullable = false)
    private int maxRentalDays = 7;

    // 사용자당 동시 대여 가능 개수
    @Column(nullable = false)
    private int maxActiveRentalsPerUser = 2;

    // 연장 가능 횟수
    @Column(nullable = false)
    private int maxExtensions = 1;

    @Builder
    public RentalPolicy(int maxRentalDays, int maxActiveRentalsPerUser, int maxExtensions) {
        this.id = 1L;
        this.maxRentalDays = maxRentalDays;
        this.maxActiveRentalsPerUser = maxActiveRentalsPerUser;
        this.maxExtensions = maxExtensions;
    }

    public void update(int maxRentalDays, int maxActiveRentalsPerUser, int maxExtensions) {
        this.maxRentalDays = maxRentalDays;
        this.maxActiveRentalsPerUser = maxActiveRentalsPerUser;
        this.maxExtensions = maxExtensions;
    }
}
