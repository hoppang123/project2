package com.company.asset.domain.reservation;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.domain.asset.*;
import com.company.asset.domain.audit.AuditAction;
import com.company.asset.domain.audit.AuditLogService;
import com.company.asset.domain.notification.NotificationService;
import com.company.asset.domain.notification.NotificationType;
import com.company.asset.domain.rental.Rental;
import com.company.asset.domain.rental.RentalItem;
import com.company.asset.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final AssetReservationRepository reservationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;

    private final AssetMaintenanceRepository maintenanceRepository;
    private final AssetHistoryRepository assetHistoryRepository;

    // ✅ 감사 로그(4번)
    private final AuditLogService auditLogService;

    // ✅ 알림(5번)
    private final NotificationService notificationService;

    /**
     * 예약 생성
     * - 예약끼리 겹침 방지
     * - 대여와 겹침 방지
     * - 점검과 겹침 방지
     */
    @Transactional
    public Long create(Long actorUserId, Long assetId, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        var user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        var asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 1) 예약 겹침 체크
        if (reservationRepository.existsOverlap(assetId, startDate, endDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 2) 대여 겹침 체크
        if (rentalRepository.existsAssetRentalOverlap(assetId, startDate, endDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 3) 점검 겹침 체크
        if (maintenanceRepository.existsOverlap(assetId, startDate, endDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        AssetReservation reservation = AssetReservation.builder()
                .asset(asset)
                .reserver(user)
                .startDate(startDate)
                .endDate(endDate)
                .status(ReservationStatus.RESERVED)
                .build();

        reservationRepository.save(reservation);

        // ✅ 감사로그
        auditLogService.log(
                actorUserId,
                AuditAction.RESERVATION_CREATE,
                "Reservation",
                reservation.getId(),
                null,
                reservation,
                "예약 생성"
        );

        // ✅ 알림
        notificationService.notify(
                actorUserId,
                NotificationType.RESERVATION_CREATED,
                "예약이 생성되었습니다.",
                "Reservation",
                reservation.getId()
        );

        return reservation.getId();
    }

    /**
     * 예약 취소(예약자 본인만)
     */
    @Transactional
    public void cancel(Long reservationId, Long actorUserId) {
        AssetReservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!r.getReserver().getId().equals(actorUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (r.getStatus() == ReservationStatus.CANCELED) return;

        // CHECKED_OUT(이미 대여로 전환)된 예약은 취소 불가(정책)
        if (r.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        r.cancel();

        // ✅ 감사로그
        auditLogService.log(
                actorUserId,
                AuditAction.RESERVATION_CANCEL,
                "Reservation",
                reservationId,
                null,
                r,
                "예약 취소"
        );

        // ✅ 알림
        notificationService.notify(
                actorUserId,
                NotificationType.RESERVATION_CANCELED,
                "예약이 취소되었습니다.",
                "Reservation",
                reservationId
        );
    }

    /**
     * (10번) 예약 → 대여 전환(체크아웃)
     * - 예약자 본인만
     * - RESERVED 상태에서만
     * - 대여/점검 겹침 재검증
     * - Asset AVAILABLE이어야 함
     * - Rental + RentalItem 생성
     * - Asset RENTED 전환 + AssetHistory 기록
     * - Reservation CHECKED_OUT + rental 연결
     */
    @Transactional
    public Long checkOut(Long reservationId, Long actorUserId) {

        AssetReservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!r.getReserver().getId().equals(actorUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (r.getStatus() != ReservationStatus.RESERVED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Long assetId = r.getAsset().getId();

        // 겹침 체크 (대여/점검)
        if (rentalRepository.existsAssetRentalOverlap(assetId, r.getStartDate(), r.getEndDate())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (maintenanceRepository.existsOverlap(assetId, r.getStartDate(), r.getEndDate())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // Asset 상태 체크
        Asset a = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (a.getStatus() != AssetStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        var actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // Rental 생성
        Rental rental = rentalRepository.save(new Rental(actor, r.getStartDate(), r.getEndDate()));

        // RentalItem 생성
        rentalItemRepository.save(new RentalItem(rental, a));

        // Asset RENTED 전환 + 이력
        AssetStatus before = a.getStatus();
        a.changeStatus(AssetStatus.RENTED);

        assetHistoryRepository.save(AssetHistory.builder()
                .asset(a)
                .action(AssetAction.STATUS_CHANGE)
                .beforeStatus(before)
                .afterStatus(a.getStatus())
                .actor(actor)
                .note("예약 체크아웃 → 대여중(RENTED)")
                .build());

        // Reservation 상태 변경 + Rental 연결
        r.checkOut(rental);

        // ✅ 감사로그
        auditLogService.log(
                actorUserId,
                AuditAction.RESERVATION_CHECKED_OUT,
                "Reservation",
                reservationId,
                null,
                r,
                "예약 → 대여 전환"
        );

        // ✅ 알림 (대여로 이동하기 쉬움)
        notificationService.notify(
                actorUserId,
                NotificationType.RESERVATION_CHECKED_OUT,
                "예약이 대여로 전환되었습니다.",
                "Rental",
                rental.getId()
        );

        return rental.getId();
    }
}