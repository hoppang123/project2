package com.company.asset.repository;

import com.company.asset.domain.analytics.UserRiskLevel;
import com.company.asset.domain.analytics.UserRiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRiskProfileRepository extends JpaRepository<UserRiskProfile, Long> {
    List<UserRiskProfile> findByLevel(UserRiskLevel level);
}