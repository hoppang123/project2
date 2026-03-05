package com.company.asset.domain.rental;

import com.company.asset.domain.department.Department;
import com.company.asset.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional = false)
    private User requester;

    @ManyToOne(fetch=FetchType.LAZY)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private RequestStatus status = RequestStatus.PENDING;

    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(fetch = FetchType.LAZY)
    private Rental rental; // 승인 완료 후 연결

    public RentalRequest(User requester, Department department, String purpose, LocalDate startDate, LocalDate endDate) {
        this.requester = requester;
        this.department = department;
        this.purpose = purpose;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void moveApproving() { this.status = RequestStatus.APPROVING; }
    public void approve() { this.status = RequestStatus.APPROVED; }
    public void reject() { this.status = RequestStatus.REJECTED; }
    public void linkRental(Rental rental) { this.rental = rental; }

    public void cancel() {
        this.status = RequestStatus.CANCELED;
    }

}
