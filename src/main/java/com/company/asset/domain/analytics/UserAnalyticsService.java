package com.company.asset.domain.analytics;

import com.company.asset.domain.rental.RentalStatus;
import com.company.asset.repository.RentalRepository;
import com.company.asset.repository.UserRepository;
import com.company.asset.repository.UserRiskProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAnalyticsService {

    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final UserRiskProfileRepository riskProfileRepository;

    /**
     * threshold: 반납률 임계값(예: 0.7)
     * minCount: 최소 대여건수(예: 3) 이상부터 분류
     */
    @Transactional
    public void recalcAll(double threshold, long minCount) {
        var users = userRepository.findAll();

        for (var u : users) {
            long total = rentalRepository.countByRenterId(u.getId());
            if (total < minCount) {
                continue; // 표본 부족 → 미분류(프로필 저장 안 함)
            }

            long returned = rentalRepository.countByStatusAndRenterId(RentalStatus.RETURNED, u.getId());
            double rate = total == 0 ? 1.0 : (returned * 1.0 / total);

            UserRiskLevel level = (rate < threshold) ? UserRiskLevel.LOW_RETURN : UserRiskLevel.NORMAL;

            riskProfileRepository.save(UserRiskProfile.builder()
                    .userId(u.getId())
                    .level(level)
                    .totalRentals(total)
                    .returnedRentals(returned)
                    .returnRate(rate)
                    .calculatedAt(LocalDateTime.now())
                    .build());
        }
    }
}