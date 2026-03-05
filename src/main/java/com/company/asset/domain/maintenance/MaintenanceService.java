package com.company.asset.domain.maintenance;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.domain.asset.Asset;
import com.company.asset.domain.asset.AssetAction;
import com.company.asset.domain.asset.AssetHistory;
import com.company.asset.domain.audit.AuditAction;
import com.company.asset.domain.audit.AuditLogService;
import com.company.asset.domain.user.Role;
import com.company.asset.domain.user.User;
import com.company.asset.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.company.asset.domain.notification.NotificationService;
import com.company.asset.domain.notification.NotificationType;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final AssetMaintenanceRepository maintenanceRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AssetHistoryRepository assetHistoryRepository;

    private final RentalRepository rentalRepository;
    private final AssetReservationRepository reservationRepository;

    // ✅ 감사 로그
    private final AuditLogService auditLogService;

    private final com.company.asset.domain.notification.NotificationService notificationService;

    @Transactional
    public Long create(Long actorUserId, Long assetId, LocalDate startDate, LocalDate endDate, String reason) {
        if (endDate.isBefore(startDate)) throw new BusinessException(ErrorCode.BAD_REQUEST);

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!(actor.getRole() == Role.ASSET_ADMIN || actor.getRole() == Role.SUPER_ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (maintenanceRepository.existsOverlap(assetId, startDate, endDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        if (reservationRepository.existsOverlap(assetId, startDate, endDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        if (rentalRepository.existsAssetRentalOverlap(assetId, startDate, endDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        AssetMaintenance m = AssetMaintenance.builder()
                .asset(asset)
                .createdBy(actor)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .status(MaintenanceStatus.PLANNED)
                .build();

        maintenanceRepository.save(m);

        assetHistoryRepository.save(AssetHistory.builder()
                .asset(asset)
                .action(AssetAction.STATUS_CHANGE)
                .beforeStatus(asset.getStatus())
                .afterStatus(asset.getStatus())
                .actor(actor)
                .note("점검 등록: " + (reason == null ? "" : reason))
                .build());

        // ✅ 감사로그
        auditLogService.log(
                actorUserId,
                AuditAction.MAINTENANCE_CREATE,
                "Maintenance",
                m.getId(),
                null,
                m,
                "점검 등록"
        );

        notificationService.notify(
                actorUserId,
                NotificationType.MAINTENANCE_CREATED,
                "점검/유지보수 일정이 등록되었습니다.",
                "Maintenance",
                m.getId()
        );

        return m.getId();
    }

    @Transactional
    public void cancel(Long maintenanceId, Long actorUserId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!(actor.getRole() == Role.ASSET_ADMIN || actor.getRole() == Role.SUPER_ADMIN)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        AssetMaintenance m = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        m.cancel();

        // ✅ 감사로그
        auditLogService.log(
                actorUserId,
                AuditAction.MAINTENANCE_CANCEL,
                "Maintenance",
                maintenanceId,
                null,
                m,
                "점검 취소"
        );

        notificationService.notify(
                actorUserId,
                NotificationType.MAINTENANCE_CANCELED,
                "점검/유지보수 일정이 취소되었습니다.",
                "Maintenance",
                maintenanceId
        );
    }
}