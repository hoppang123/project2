package com.company.asset.domain.stats;

import com.company.asset.domain.rental.RentalStatus;
import com.company.asset.repository.AssetMaintenanceRepository;
import com.company.asset.repository.RentalRepository;
import com.company.asset.web.stats.dto.AdminStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final RentalRepository rentalRepository;

    // ✅ 있으면 집계, 없으면 0 처리 (프로젝트 상태에 따라 유연하게)
    private final ObjectProvider<com.company.asset.repository.AssetReservationRepository> reservationRepositoryProvider;
    private final ObjectProvider<AssetMaintenanceRepository> maintenanceRepositoryProvider;

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats(int days) {
        int d = Math.max(1, Math.min(days, 90)); // 1~90일 제한

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        long active = rentalRepository.countByStatus(RentalStatus.ACTIVE);
        long overdue = rentalRepository.countByStatus(RentalStatus.OVERDUE);
        long returnRequested = rentalRepository.countByStatus(RentalStatus.RETURN_REQUESTED);

        long todayIssued = rentalRepository.countByIssuedAtBetween(todayStart, tomorrowStart);
        long todayReturned = rentalRepository.countByReturnedAtBetween(todayStart, tomorrowStart);

        var reservationRepo = reservationRepositoryProvider.getIfAvailable();
        var maintenanceRepo = maintenanceRepositoryProvider.getIfAvailable();

        long todayReservations = (reservationRepo == null)
                ? 0
                : reservationRepo.countByCreatedAtBetween(todayStart, tomorrowStart);

        long todayMaintenances = (maintenanceRepo == null)
                ? 0
                : maintenanceRepo.countByCreatedAtBetween(todayStart, tomorrowStart);

        // series range: [from, to)
        LocalDateTime from = today.minusDays(d - 1).atStartOfDay();
        LocalDateTime to = tomorrowStart;

        // date -> series point
        Map<LocalDate, AdminStatsResponse.DailySeriesPoint> map = new LinkedHashMap<>();
        for (int i = 0; i < d; i++) {
            LocalDate date = today.minusDays(d - 1 - i);
            map.put(date, new AdminStatsResponse.DailySeriesPoint(date.toString(), 0, 0, 0, 0));
        }

        // rentals started (issuedAt)
        for (Object[] row : rentalRepository.countDailyIssuedAt(from, to)) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            long cnt = ((Number) row[1]).longValue();
            var prev = map.get(date);
            if (prev != null) {
                map.put(date, new AdminStatsResponse.DailySeriesPoint(prev.date(), cnt, prev.returnsConfirmed(), prev.reservationsCreated(), prev.maintenancesCreated()));
            }
        }

        // returns confirmed (returnedAt)
        for (Object[] row : rentalRepository.countDailyReturnedAt(from, to)) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            long cnt = ((Number) row[1]).longValue();
            var prev = map.get(date);
            if (prev != null) {
                map.put(date, new AdminStatsResponse.DailySeriesPoint(prev.date(), prev.rentalsStarted(), cnt, prev.reservationsCreated(), prev.maintenancesCreated()));
            }
        }

        // reservations created
        if (reservationRepo != null) {
            for (Object[] row : reservationRepo.countDailyCreatedAt(from, to)) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                long cnt = ((Number) row[1]).longValue();
                var prev = map.get(date);
                if (prev != null) {
                    map.put(date, new AdminStatsResponse.DailySeriesPoint(prev.date(), prev.rentalsStarted(), prev.returnsConfirmed(), cnt, prev.maintenancesCreated()));
                }
            }
        }

        // maintenances created
        if (maintenanceRepo != null) {
            for (Object[] row : maintenanceRepo.countDailyCreatedAt(from, to)) {
                LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                long cnt = ((Number) row[1]).longValue();
                var prev = map.get(date);
                if (prev != null) {
                    map.put(date, new AdminStatsResponse.DailySeriesPoint(prev.date(), prev.rentalsStarted(), prev.returnsConfirmed(), prev.reservationsCreated(), cnt));
                }
            }
        }

        return new AdminStatsResponse(
                active, overdue, returnRequested,
                todayIssued, todayReturned, todayReservations, todayMaintenances,
                new ArrayList<>(map.values())
        );
    }
}