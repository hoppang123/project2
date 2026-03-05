package com.company.asset.domain.rental.scheduler;

import com.company.asset.domain.rental.Rental;
import com.company.asset.domain.rental.RentalStatus;
import com.company.asset.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final RentalRepository rentalRepository;

    // 로컬 테스트: 10분마다
    @Scheduled(fixedDelay = 10 * 60 * 1000L)
    @Transactional
    public void markOverdue() {
        LocalDate today = LocalDate.now();

        List<Rental> targets =
                rentalRepository.findByStatusInAndEndDateBeforeAndReturnedAtIsNull(
                        List.of(RentalStatus.ACTIVE),
                        today
                );

        targets.forEach(Rental::markOverdue);
    }
}