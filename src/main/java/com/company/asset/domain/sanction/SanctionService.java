package com.company.asset.domain.sanction;

import com.company.asset.repository.AssetSanctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SanctionService {

    private final AssetSanctionRepository sanctionRepository;

    public void assertNotSanctioned(Long userId) {
        boolean blocked = sanctionRepository.existsByUserIdAndStatusAndEndsAtAfter(
                userId, SanctionStatus.ACTIVE, LocalDateTime.now()
        );
        if (blocked) {
            throw new IllegalStateException("대여 제한 상태입니다(제재 기간 중).");
        }
    }

    @Transactional
    public void expireEndedSanctions() {
        LocalDateTime now = LocalDateTime.now();
        sanctionRepository.findByStatusAndEndsAtBefore(SanctionStatus.ACTIVE, now)
                .forEach(AssetSanction::expire);
    }
}