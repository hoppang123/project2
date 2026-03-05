package com.company.asset.domain.asset;

import com.company.asset.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetAction action;

    @Enumerated(EnumType.STRING)
    private AssetStatus beforeStatus;

    @Enumerated(EnumType.STRING)
    private AssetStatus afterStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private User actor;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public AssetHistory(Asset asset, AssetAction action, AssetStatus beforeStatus, AssetStatus afterStatus, User actor, String note) {
        this.asset = asset;
        this.action = action;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.actor = actor;
        this.note = note;
    }
}
