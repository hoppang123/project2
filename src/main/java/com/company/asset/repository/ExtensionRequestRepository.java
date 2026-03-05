package com.company.asset.repository;

import com.company.asset.domain.rental.ExtensionRequest;
import com.company.asset.domain.rental.ExtensionStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ExtensionRequestRepository extends JpaRepository<ExtensionRequest, Long> {
    Page<ExtensionRequest> findByStatus(ExtensionStatus status, Pageable pageable);
    Page<ExtensionRequest> findByRequesterId(Long requesterId, Pageable pageable);

    Optional<ExtensionRequest> findTopByRentalIdAndStatusOrderByIdDesc(Long rentalId, ExtensionStatus status);

    long countByStatus(ExtensionStatus status);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}