package com.company.asset.repository;

import com.company.asset.domain.rental.RentalRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRequestItemRepository extends JpaRepository<RentalRequestItem, Long> {
    List<RentalRequestItem> findByRequestId(Long requestId);
}
