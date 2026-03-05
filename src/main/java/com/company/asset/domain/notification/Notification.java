package com.company.asset.domain.notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notif_user_status", columnList = "userId,status,createdAt")
        }
)
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(nullable = false, length = 255)
    private String message;

    // 프론트가 바로 이동할 수 있게 대상 링크 정보(선택)
    @Column(length = 60)
    private String targetType; // e.g. RentalRequest, Rental, Reservation

    private Long targetId;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime readAt;

    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }
}