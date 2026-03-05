package com.company.asset.domain.rental;

import com.company.asset.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rental {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User renter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status = RentalStatus.ACTIVE;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    private LocalDateTime returnedAt;

    public Rental(User renter, LocalDate startDate, LocalDate endDate) {
        this.renter = renter;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void requestReturn() { this.status = RentalStatus.RETURN_REQUESTED; }
    public void confirmReturn() {
        this.status = RentalStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }

    @Column(nullable = false)
    private int extensionCount = 0;

    public int getExtensionCount() { return extensionCount; }

    public void extendEndDate(java.time.LocalDate newEndDate) {
        this.endDate = newEndDate;
        this.extensionCount++;
    }

    public void markOverdue() {
        if (this.status == RentalStatus.ACTIVE && this.returnedAt == null) {
            this.status = RentalStatus.OVERDUE;
        }
    }
}
