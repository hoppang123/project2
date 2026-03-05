package com.company.asset.domain.rental;

import com.company.asset.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtensionRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User requester;

    @Column(nullable = false)
    private LocalDate requestedEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExtensionStatus status = ExtensionStatus.PENDING;

    private String reason;
    private String adminNote;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime actedAt;

    public ExtensionRequest(Rental rental, User requester, LocalDate requestedEndDate, String reason) {
        this.rental = rental;
        this.requester = requester;
        this.requestedEndDate = requestedEndDate;
        this.reason = reason;
    }

    public void approve(String adminNote) {
        this.status = ExtensionStatus.APPROVED;
        this.adminNote = adminNote;
        this.actedAt = LocalDateTime.now();
    }

    public void reject(String adminNote) {
        this.status = ExtensionStatus.REJECTED;
        this.adminNote = adminNote;
        this.actedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = ExtensionStatus.CANCELED;
        this.actedAt = LocalDateTime.now();
    }
}
