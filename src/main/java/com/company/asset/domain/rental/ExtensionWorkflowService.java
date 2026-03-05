package com.company.asset.domain.rental;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.domain.policy.RentalPolicy;
import com.company.asset.domain.policy.RentalPolicyService;
import com.company.asset.domain.user.Role;
import com.company.asset.domain.user.User;
import com.company.asset.repository.ExtensionRequestRepository;
import com.company.asset.repository.RentalRepository;
import com.company.asset.repository.UserRepository;
import com.company.asset.web.extension.dto.ExtensionCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ExtensionWorkflowService {

    private final ExtensionRequestRepository extensionRequestRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final RentalPolicyService policyService;

    /**
     * 연장 요청(대여자 본인)
     * - rental ACTIVE/RETURN_REQUESTED 상태에서만 허용(RETURNED면 불가)
     * - requestedEndDate는 현재 endDate보다 뒤여야 함
     * - 정책: maxExtensions 초과 불가
     * - 정책: maxRentalDays 초과 불가(시작~연장종료 총합)
     * - 이미 PENDING 연장요청이 있으면 중복 불가
     */
    @Transactional
    public Long requestExtension(ExtensionCreateRequest req, Long actorId) {
        Rental rental = rentalRepository.findById(req.getRentalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!rental.getRenter().getId().equals(actorId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (rental.getStatus() == RentalStatus.RETURNED) throw new BusinessException(ErrorCode.BAD_REQUEST);

        // requestedEndDate 검증
        if (req.getRequestedEndDate() == null || rental.getEndDate() == null) throw new BusinessException(ErrorCode.BAD_REQUEST);
        if (!req.getRequestedEndDate().isAfter(rental.getEndDate())) throw new BusinessException(ErrorCode.BAD_REQUEST);

        // 중복 pending 방지
        extensionRequestRepository.findTopByRentalIdAndStatusOrderByIdDesc(rental.getId(), ExtensionStatus.PENDING)
                .ifPresent(x -> { throw new BusinessException(ErrorCode.BAD_REQUEST); });

        RentalPolicy policy = policyService.getOrCreateDefault();

        // 연장 횟수 정책
        if (rental.getExtensionCount() >= policy.getMaxExtensions()) throw new BusinessException(ErrorCode.BAD_REQUEST);

        // 총 대여기간 정책(시작~요청 종료)
        long totalDays = ChronoUnit.DAYS.between(rental.getStartDate(), req.getRequestedEndDate()) + 1;
        if (totalDays > policy.getMaxRentalDays()) throw new BusinessException(ErrorCode.BAD_REQUEST);

        User requester = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        ExtensionRequest er = new ExtensionRequest(rental, requester, req.getRequestedEndDate(), req.getReason());
        extensionRequestRepository.save(er);

        return er.getId();
    }

    /**
     * 연장 승인(자산관리자 이상)
     * - APPROVED 처리 + rental.endDate 변경 + extensionCount 증가
     */
    @Transactional
    public void approve(Long extensionRequestId, Long actorId, String adminNote) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (actor.getRole() != Role.ASSET_ADMIN && actor.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        ExtensionRequest er = extensionRequestRepository.findById(extensionRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (er.getStatus() != ExtensionStatus.PENDING) throw new BusinessException(ErrorCode.BAD_REQUEST);

        Rental rental = er.getRental();
        if (rental.getStatus() == RentalStatus.RETURNED) throw new BusinessException(ErrorCode.BAD_REQUEST);

        RentalPolicy policy = policyService.getOrCreateDefault();

        if (rental.getExtensionCount() >= policy.getMaxExtensions()) throw new BusinessException(ErrorCode.BAD_REQUEST);

        long totalDays = ChronoUnit.DAYS.between(rental.getStartDate(), er.getRequestedEndDate()) + 1;
        if (totalDays > policy.getMaxRentalDays()) throw new BusinessException(ErrorCode.BAD_REQUEST);

        // 승인 처리
        er.approve(adminNote);
        rental.extendEndDate(er.getRequestedEndDate());
    }

    /**
     * 연장 반려(자산관리자 이상)
     */
    @Transactional
    public void reject(Long extensionRequestId, Long actorId, String adminNote) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (actor.getRole() != Role.ASSET_ADMIN && actor.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        ExtensionRequest er = extensionRequestRepository.findById(extensionRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (er.getStatus() != ExtensionStatus.PENDING) throw new BusinessException(ErrorCode.BAD_REQUEST);

        er.reject(adminNote);
    }

    /**
     * 연장 요청 취소(요청자 본인)
     */
    @Transactional
    public void cancel(Long extensionRequestId, Long actorId) {
        ExtensionRequest er = extensionRequestRepository.findById(extensionRequestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!er.getRequester().getId().equals(actorId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (er.getStatus() != ExtensionStatus.PENDING) throw new BusinessException(ErrorCode.BAD_REQUEST);

        er.cancel();
    }
}
