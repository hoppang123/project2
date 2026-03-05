package com.company.asset.repository;

import com.company.asset.domain.rental.RentalRequest;
import com.company.asset.domain.rental.RequestStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    Page<RentalRequest> findByRequesterId(Long requesterId, Pageable pageable);
    Page<RentalRequest> findByStatus(RequestStatus status, Pageable pageable);
    long countByStatus(RequestStatus status);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
