package com.company.asset.domain.analytics.scheduler;

import com.company.asset.domain.analytics.UserAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRiskScheduler {

    private final UserAnalyticsService analyticsService;

    // 매일 03:10 (운영용). 로컬은 fixedDelay로 바꿔도 됨.
    @Scheduled(cron = "0 10 3 * * *")
    public void recalc() {
        analyticsService.recalcAll(0.7, 3);
    }
}