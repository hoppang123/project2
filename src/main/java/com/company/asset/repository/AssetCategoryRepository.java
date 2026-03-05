package com.company.asset.repository;

import com.company.asset.domain.asset.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {
    Optional<AssetCategory> findByName(String name);
}
