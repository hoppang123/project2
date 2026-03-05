package com.company.asset.web.reservation;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.reservation.ReservationQueryService;
import com.company.asset.domain.reservation.ReservationService;
import com.company.asset.security.auth.CustomUserDetails;
import com.company.asset.web.reservation.dto.ReservationCreateRequest;
import com.company.asset.web.reservation.dto.ReservationSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationQueryService reservationQueryService;

    /**
     * 예약 생성
     */
    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody ReservationCreateRequest req,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        Long id = reservationService.create(
                principal.getUserId(),
                req.getAssetId(),
                req.getStartDate(),
                req.getEndDate()
        );
        return ApiResponse.ok(id);
    }

    /**
     * 예약 취소(본인)
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails principal) {
        reservationService.cancel(id, principal.getUserId());
        return ApiResponse.ok();
    }

    /**
     * ✅ 내 예약 목록(페이지)
     * GET /api/reservations/me?page=0&size=20
     */
    @GetMapping("/me")
    public ApiResponse<Page<ReservationSummaryResponse>> myReservations(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(reservationQueryService.myReservations(principal.getUserId(), page, size));
    }

    /**
     * ✅ 예약 → 대여 전환(checkout)
     * - 성공 시 rentalId 반환
     */
    @PostMapping("/{id}/checkout")
    public ApiResponse<Long> checkout(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails principal) {
        Long rentalId = reservationService.checkOut(id, principal.getUserId());
        return ApiResponse.ok(rentalId);
    }
}