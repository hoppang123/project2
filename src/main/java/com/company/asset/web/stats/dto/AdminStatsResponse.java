package com.company.asset.web.stats.dto;

import java.util.List;

public record AdminStatsResponse(
        long activeRentals,
        long overdueRentals,
        long returnRequestedRentals,

        long todayRentalsStarted,
        long todayReturnsConfirmed,
        long todayReservationsCreated,
        long todayMaintenancesCreated,

        List<DailySeriesPoint> series
) {
    public record DailySeriesPoint(
            String date, // yyyy-MM-dd
            long rentalsStarted,
            long returnsConfirmed,
            long reservationsCreated,
            long maintenancesCreated
    ) {}
}