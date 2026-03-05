package com.company.asset.repository;

import com.company.asset.domain.policy.RentalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalPolicyRepository extends JpaRepository<RentalPolicy, Long> {
}
