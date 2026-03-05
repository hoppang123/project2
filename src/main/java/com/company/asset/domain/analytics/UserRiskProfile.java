package com.company.asset.domain.analytics;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "user_risk_profiles")
public class UserRiskProfile {

    @Id
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRiskLevel level;

    @Column(nullable = false)
    private long totalRentals;

    @Column(nullable = false)
    private long returnedRentals;

    @Column(nullable = false)
    private double returnRate;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;
}