package com.company.asset.domain.rental;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.domain.approval.*;
import com.company.asset.domain.asset.*;
import com.company.asset.domain.audit.AuditAction;
import com.company.asset.domain.audit.AuditLogService;
import com.company.asset.domain.sanction.AssetSanction;
import com.company.asset.domain.sanction.SanctionReason;
import com.company.asset.domain.sanction.SanctionService;
import com.company.asset.domain.user.Role;
import com.company.asset.domain.user.User;
import com.company.asset.repository.*;
import com.company.asset.web.rental.dto.RentalRequestCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.company.asset.domain.notification.NotificationService;
import com.company.asset.domain.notification.NotificationType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalWorkflowService {

    private final RentalRequestRepository requestRepository;
    private final RentalRequestItemRepository requestItemRepository;
    private final ApprovalStepRepository approvalStepRepository;

    private final AssetRepository assetRepository;
    private final AssetHistoryRepository assetHistoryRepository;

    private final UserRepository userRepository;

    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;

    // ✅ 제재(1번 기능)
    private final SanctionService sanctionService;
    private final AssetSanctionRepository assetSanctionRepository;

    // ✅ 점검(3번 기능) - 대여 신청 시 점검 기간과 겹치면 신청 차단
    private final AssetMaintenanceRepository maintenanceRepository;

    // ✅ 감사 로그(4번 기능)
    private final AuditLogService auditLogService;

    private final com.company.asset.domain.notification.NotificationService notificationService;

    /**
     * 1) 대여 신청
     */
    @Transactional
    public Long createRequest(RentalRequestCreateRequest req, Long requesterId) {

        // 제재 체크
        sanctionService.assertNotSanctioned(requesterId);

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<Long> assetIds = req.getAssetIds();
        List<Asset> assets = assetRepository.findAllById(assetIds);
        if (assets.size() != assetIds.size()) throw new BusinessException(ErrorCode.NOT_FOUND);

        // 상태 체크 + 점검 겹침 체크
        for (Asset a : assets) {
            if (a.getStatus() != AssetStatus.AVAILABLE) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            if (maintenanceRepository.existsOverlap(a.getId(), req.getStartDate(), req.getEndDate())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
        }

        // 요청 생성
        RentalRequest request = new RentalRequest(
                requester,
                null,
                req.getPurpose(),
                req.getStartDate(),
                req.getEndDate()
        );
        request.moveApproving();
        requestRepository.save(request);

        // 요청 아이템 + 자산 RESERVED 처리
        for (Asset a : assets) {
            requestItemRepository.save(new RentalRequestItem(request, a));

            AssetStatus before = a.getStatus();
            a.changeStatus(AssetStatus.RESERVED);

            assetHistoryRepository.save(AssetHistory.builder()
                    .asset(a)
                    .action(AssetAction.STATUS_CHANGE)
                    .beforeStatus(before)
                    .afterStatus(a.getStatus())
                    .actor(requester)
                    .note("대여 신청으로 예약(RESERVED)")
                    .build());
        }

        // 승인 단계 생성(1차 MANAGER, 2차 ASSET_ADMIN)
        approvalStepRepository.save(new ApprovalStep(request, 1, Role.MANAGER));
        approvalStepRepository.save(new ApprovalStep(request, 2, Role.ASSET_ADMIN));

        // ✅ 감사로그
        auditLogService.log(
                requesterId,
                AuditAction.RENTAL_REQUEST_CREATE,
                "RentalRequest",
                request.getId(),
                null,
                request,
                "대여 신청 생성"
        );

        notificationService.notify(
                requesterId,
                NotificationType.RENTAL_REQUEST_CREATED,
                "대여 신청이 접수되었습니다.",
                "RentalRequest",
                request.getId()
        );

        return request.getId();
    }

    /**
     * 2) 승인
     */
    @Transactional
    public void approve(Long requestId, Long actorId, String note) {
        RentalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        ApprovalStep step = approvalStepRepository
                .findFirstByRequestIdAndStatusOrderByStepNoAsc(requestId, ApprovalStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

        if (step.getApproverRole() != actor.getRole()) throw new BusinessException(ErrorCode.FORBIDDEN);

        step.approve(note);

        notificationService.notify(
                request.getRequester().getId(),
                NotificationType.RENTAL_REQUEST_APPROVED,
                "대여 신청이 승인되었습니다. (단계: " + step.getStepNo() + ")",
                "RentalRequest",
                requestId
        );

        // ✅ 감사로그: 단계 승인
        auditLogService.log(
                actorId,
                AuditAction.RENTAL_REQUEST_APPROVE_STEP,
                "RentalRequest",
                requestId,
                null,
                step,
                "승인 단계 승인: stepNo=" + step.getStepNo()
        );

        boolean hasNext = approvalStepRepository
                .findFirstByRequestIdAndStatusOrderByStepNoAsc(requestId, ApprovalStatus.PENDING)
                .isPresent();

        if (!hasNext) {
            request.approve();

            Rental rental = rentalRepository.save(
                    new Rental(request.getRequester(), request.getStartDate(), request.getEndDate())
            );
            request.linkRental(rental);

            List<RentalRequestItem> items = requestItemRepository.findByRequestId(requestId);
            for (RentalRequestItem ri : items) {
                Asset a = ri.getAsset();

                AssetStatus before = a.getStatus();
                a.changeStatus(AssetStatus.RENTED);

                rentalItemRepository.save(new RentalItem(rental, a));

                assetHistoryRepository.save(AssetHistory.builder()
                        .asset(a)
                        .action(AssetAction.STATUS_CHANGE)
                        .beforeStatus(before)
                        .afterStatus(a.getStatus())
                        .actor(actor)
                        .note("최종 승인 완료 → 대여중(RENTED)")
                        .build());
            }

            // ✅ 감사로그: 최종 승인 완료 → Rental 생성
            auditLogService.log(
                    actorId,
                    AuditAction.RENTAL_REQUEST_APPROVED_FINAL,
                    "Rental",
                    rental.getId(),
                    null,
                    rental,
                    "최종 승인 완료 → Rental 생성"
            );

            notificationService.notify(
                    request.getRequester().getId(),
                    NotificationType.RENTAL_REQUEST_APPROVED,
                    "대여 신청이 최종 승인되었습니다. 대여가 시작되었습니다.",
                    "Rental",
                    rental.getId()
            );
        }
    }

    /**
     * 3) 반려
     */
    @Transactional
    public void reject(Long requestId, Long actorId, String reason) {
        RentalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        ApprovalStep step = approvalStepRepository
                .findFirstByRequestIdAndStatusOrderByStepNoAsc(requestId, ApprovalStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST));

        if (step.getApproverRole() != actor.getRole()) throw new BusinessException(ErrorCode.FORBIDDEN);

        step.reject(reason);
        request.reject();

        List<RentalRequestItem> items = requestItemRepository.findByRequestId(requestId);
        for (RentalRequestItem ri : items) {
            Asset a = ri.getAsset();
            if (a.getStatus() == AssetStatus.RESERVED) {
                AssetStatus before = a.getStatus();
                a.changeStatus(AssetStatus.AVAILABLE);

                assetHistoryRepository.save(AssetHistory.builder()
                        .asset(a)
                        .action(AssetAction.STATUS_CHANGE)
                        .beforeStatus(before)
                        .afterStatus(a.getStatus())
                        .actor(actor)
                        .note("요청 반려 → 예약 해제(AVAILABLE)")
                        .build());
            }
        }

        // ✅ 감사로그
        auditLogService.log(
                actorId,
                AuditAction.RENTAL_REQUEST_REJECT,
                "RentalRequest",
                requestId,
                null,
                request,
                reason
        );

        notificationService.notify(
                request.getRequester().getId(),
                NotificationType.RENTAL_REQUEST_REJECTED,
                "대여 신청이 반려되었습니다. 사유: " + reason,
                "RentalRequest",
                requestId
        );
    }

    /**
     * 4) 요청 취소(요청자 본인만)
     */
    @Transactional
    public void cancelRequest(Long requestId, Long actorId) {
        RentalRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!request.getRequester().getId().equals(actorId)) throw new BusinessException(ErrorCode.FORBIDDEN);

        if (request.getStatus() != RequestStatus.APPROVING && request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        request.cancel();

        List<RentalRequestItem> items = requestItemRepository.findByRequestId(requestId);
        User actor = userRepository.findById(actorId).orElse(null);

        for (RentalRequestItem ri : items) {
            Asset a = ri.getAsset();
            if (a.getStatus() == AssetStatus.RESERVED) {
                AssetStatus before = a.getStatus();
                a.changeStatus(AssetStatus.AVAILABLE);

                assetHistoryRepository.save(AssetHistory.builder()
                        .asset(a)
                        .action(AssetAction.STATUS_CHANGE)
                        .beforeStatus(before)
                        .afterStatus(a.getStatus())
                        .actor(actor)
                        .note("요청 취소 → 예약 해제(AVAILABLE)")
                        .build());
            }
        }

        // ✅ 감사로그
        auditLogService.log(
                actorId,
                AuditAction.RENTAL_REQUEST_CANCEL,
                "RentalRequest",
                requestId,
                null,
                request,
                "요청 취소"
        );

        notificationService.notify(
                actorId,
                NotificationType.RENTAL_REQUEST_CANCELED, // ✅ 아래 "타입 추가" 참고
                "대여 신청이 취소되었습니다.",
                "RentalRequest",
                requestId
        );
    }

    /**
     * 5) 반납 요청(대여자 본인만)
     */
    @Transactional
    public void requestReturn(Long rentalId, Long actorId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!rental.getRenter().getId().equals(actorId)) throw new BusinessException(ErrorCode.FORBIDDEN);

        if (rental.getStatus() == RentalStatus.RETURNED) return;
        if (rental.getStatus() == RentalStatus.RETURN_REQUESTED) return;

        if (!(rental.getStatus() == RentalStatus.ACTIVE || rental.getStatus() == RentalStatus.OVERDUE)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        rental.requestReturn();

        // ✅ 감사로그
        auditLogService.log(
                actorId,
                AuditAction.RENTAL_RETURN_REQUEST,
                "Rental",
                rentalId,
                null,
                rental,
                "반납 요청"
        );

        notificationService.notify(
                actorId,
                NotificationType.RENTAL_RETURN_REQUESTED,
                "반납 요청이 접수되었습니다. 자산관리자의 확인을 기다려주세요.",
                "Rental",
                rentalId
        );
    }

    /**
     * 6) 반납 확인(자산관리자 이상)
     */
    @Transactional
    public void confirmReturn(Long rentalId, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (actor.getRole() != Role.ASSET_ADMIN && actor.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (rental.getStatus() == RentalStatus.RETURNED) return;

        if (rental.getStatus() != RentalStatus.RETURN_REQUESTED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        rental.confirmReturn();

        List<RentalItem> items = rentalItemRepository.findByRentalId(rentalId);
        for (RentalItem item : items) {
            item.markReturned();

            Asset a = item.getAsset();
            AssetStatus before = a.getStatus();
            a.changeStatus(AssetStatus.AVAILABLE);

            assetHistoryRepository.save(AssetHistory.builder()
                    .asset(a)
                    .action(AssetAction.STATUS_CHANGE)
                    .beforeStatus(before)
                    .afterStatus(a.getStatus())
                    .actor(actor)
                    .note("반납 확인 → 사용 가능(AVAILABLE)")
                    .build());
        }

        // 연체 제재 생성
        long overdueDays = 0;
        if (rental.getEndDate() != null && rental.getReturnedAt() != null) {
            overdueDays = ChronoUnit.DAYS.between(
                    rental.getEndDate(),
                    rental.getReturnedAt().toLocalDate()
            );
            if (overdueDays < 0) overdueDays = 0;
        }

        if (overdueDays > 0) {
            long banDays = (overdueDays >= 7) ? 7 : (overdueDays >= 3 ? 3 : 1);

            AssetSanction sanction = AssetSanction.builder()
                    .userId(rental.getRenter().getId())
                    .reason(SanctionReason.OVERDUE)
                    .status(com.company.asset.domain.sanction.SanctionStatus.ACTIVE)
                    .startsAt(LocalDateTime.now())
                    .endsAt(LocalDateTime.now().plusDays(banDays))
                    .points((int) overdueDays)
                    .memo("연체 " + overdueDays + "일 → 대여 제한 " + banDays + "일")
                    .createdBy(actorId)
                    .build();

            assetSanctionRepository.save(sanction);

            // ✅ 감사로그: 제재 생성
            auditLogService.log(
                    actorId,
                    AuditAction.SANCTION_CREATED,
                    "Sanction",
                    sanction.getId(),
                    null,
                    sanction,
                    "연체 제재 생성"
            );

            notificationService.notify(
                    rental.getRenter().getId(),
                    NotificationType.SANCTION_CREATED,
                    "연체로 인해 대여 제한이 적용되었습니다. (" + overdueDays + "일 연체)",
                    "Sanction",
                    sanction.getId()
            );
        }

        // ✅ 감사로그: 반납 확정
        auditLogService.log(
                actorId,
                AuditAction.RENTAL_RETURN_CONFIRM,
                "Rental",
                rentalId,
                null,
                rental,
                "반납 확정"
        );

        notificationService.notify(
                rental.getRenter().getId(),
                NotificationType.RENTAL_RETURN_CONFIRMED,
                "반납이 확인되었습니다. 이용해주셔서 감사합니다.",
                "Rental",
                rentalId
        );

    }
}