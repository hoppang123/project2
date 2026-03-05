package com.company.asset.web.dashboard;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.asset.AssetStatus;
import com.company.asset.domain.rental.ExtensionStatus;
import com.company.asset.domain.rental.RentalStatus;
import com.company.asset.domain.rental.RequestStatus;
import com.company.asset.repository.AssetRepository;
import com.company.asset.repository.ExtensionRequestRepository;
import com.company.asset.repository.RentalRepository;
import com.company.asset.repository.RentalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AssetRepository assetRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final RentalRepository rentalRepository;
    private final ExtensionRequestRepository extensionRequestRepository; // 없으면 주입/필드 제거

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary() {

        // 자산 상태별
        long available = assetRepository.countByStatus(AssetStatus.AVAILABLE);
        long reserved = assetRepository.countByStatus(AssetStatus.RESERVED);
        long rented = assetRepository.countByStatus(AssetStatus.RENTED);
        long maintenance = assetRepository.countByStatus(AssetStatus.MAINTENANCE);

        // 대여요청 상태별
        long reqApproving = rentalRequestRepository.countByStatus(RequestStatus.APPROVING);
        long reqApproved = rentalRequestRepository.countByStatus(RequestStatus.APPROVED);
        long reqRejected = rentalRequestRepository.countByStatus(RequestStatus.REJECTED);
        long reqCanceled = rentalRequestRepository.countByStatus(RequestStatus.CANCELED);

        // 대여 상태별
        long rentalActive = rentalRepository.countByStatus(RentalStatus.ACTIVE);
        long rentalReturnRequested = rentalRepository.countByStatus(RentalStatus.RETURN_REQUESTED);
        long rentalReturned = rentalRepository.countByStatus(RentalStatus.RETURNED);

        // 오늘 통계(오늘 00:00~내일 00:00)
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        long todayRequests = rentalRequestRepository.countByCreatedAtBetween(from, to);
        long todayExtensionRequests = (extensionRequestRepository != null)
                ? extensionRequestRepository.countByCreatedAtBetween(from, to)
                : 0;

        long extPending = (extensionRequestRepository != null)
                ? extensionRequestRepository.countByStatus(ExtensionStatus.PENDING)
                : 0;

        return ApiResponse.ok(new DashboardSummaryResponse(
                // assets
                available, reserved, rented, maintenance,
                // requests
                reqApproving, reqApproved, reqRejected, reqCanceled,
                // rentals
                rentalActive, rentalReturnRequested, rentalReturned,
                // extension
                extPending,
                // today
                todayRequests, todayExtensionRequests
        ));
    }

    public record DashboardSummaryResponse(
            long assetAvailable,
            long assetReserved,
            long assetRented,
            long assetMaintenance,

            long requestApproving,
            long requestApproved,
            long requestRejected,
            long requestCanceled,

            long rentalActive,
            long rentalReturnRequested,
            long rentalReturned,

            long extensionPending,

            long todayRequestCount,
            long todayExtensionRequestCount
    ) {}
}