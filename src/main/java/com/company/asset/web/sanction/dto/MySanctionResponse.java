package com.company.asset.web.sanction.dto;

import java.time.LocalDateTime;

public record MySanctionResponse(
        boolean sanctioned,
        String reason,
        String status,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        int points,
        String memo
) {}