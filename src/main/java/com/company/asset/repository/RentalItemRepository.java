package com.company.asset.repository;

import com.company.asset.domain.rental.RentalItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalItemRepository extends JpaRepository<RentalItem, Long> {
    List<RentalItem> findByRentalId(Long rentalId);
}
