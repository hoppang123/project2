package com.company.asset.domain.rental;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.domain.approval.ApprovalStatus;
import com.company.asset.domain.approval.ApprovalStep;
import com.company.asset.domain.user.Role;
import com.company.asset.repository.*;
import com.company.asset.web.rental.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalQueryService {

    private final RentalRequestRepository requestRepository;
    private final RentalRequestItemRepository requestItemRepository;
    private final ApprovalStepRepository approvalStepRepository;

    private final RentalRepository rentalRepository;
    private final RentalItemRepository rentalItemRepository;

    /**
     * 내 대여 신청 목록
     */
    @Transactional(readOnly = true)
    public Page<RentalRequestSummaryResponse> myRequests(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return requestRepository.findByRequesterId(userId, pageable)
                .map(r -> new RentalRequestSummaryResponse(
                        r.getId(),
                        r.getStatus(),
                        r.getPurpose(),
                        r.getStartDate(),
                        r.getEndDate(),
                        requestItemRepository.findByRequestId(r.getId()).size(),
                        r.getCreatedAt()
                ));
    }

    /**
     * 대여 신청 상세(아이템/승인 단계 포함)
     */
    @Transactional(readOnly = true)
    public RentalRequestDetailResponse requestDetail(Long requestId) {
        RentalRequest r = requestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<RentalRequestItemResponse> items = requestItemRepository.findByRequestId(requestId).stream()
                .map(ri -> new RentalRequestItemResponse(
                        ri.getAsset().getId(),
                        ri.getAsset().getAssetCode(),
                        ri.getAsset().getName(),
                        ri.getAsset().getStatus()
                ))
                .toList();

        List<ApprovalStepResponse> steps = approvalStepRepository.findByRequestIdOrderByStepNoAsc(requestId).stream()
                .map(s -> new ApprovalStepResponse(
                        s.getStepNo(),
                        s.getApproverRole().name(),
                        s.getStatus(),
                        s.getReason(),
                        s.getActedAt()
                ))
                .toList();

        Long rentalId = (r.getRental() != null) ? r.getRental().getId() : null;

        return new RentalRequestDetailResponse(
                r.getId(),
                r.getStatus(),
                r.getRequester().getEmail(),
                r.getPurpose(),
                r.getStartDate(),
                r.getEndDate(),
                r.getCreatedAt(),
                rentalId,
                items,
                steps
        );
    }

    /**
     * 승인자 "대기함" (MVP 버전)
     * - status=APPROVING 같은 조건으로 전체를 가져오는 단순 버전
     */
    @Transactional(readOnly = true)
    public Page<RentalRequestSummaryResponse> pendingBox(RequestStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return requestRepository.findByStatus(status, pageable)
                .map(r -> new RentalRequestSummaryResponse(
                        r.getId(),
                        r.getStatus(),
                        r.getPurpose(),
                        r.getStartDate(),
                        r.getEndDate(),
                        requestItemRepository.findByRequestId(r.getId()).size(),
                        r.getCreatedAt()
                ));
    }

    /**
     * ✅ 승인자 "내 승인함" (기본 버전)
     * - 내 역할(Role)과 일치하는 PENDING step
     * - 요청(Request)은 APPROVING 상태
     * - 프론트가 requestId로 상세 들어가서 처리
     */
    @Transactional(readOnly = true)
    public Page<RentalRequestSummaryResponse> myApprovalInbox(Role myRole, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<ApprovalStep> steps = approvalStepRepository.findPendingStepsByRole(
                myRole,
                ApprovalStatus.PENDING,
                pageable
        );

        return steps.map(s -> {
            var r = s.getRequest();
            int itemCount = requestItemRepository.findByRequestId(r.getId()).size();

            return new RentalRequestSummaryResponse(
                    r.getId(),
                    r.getStatus(),
                    r.getPurpose(),
                    r.getStartDate(),
                    r.getEndDate(),
                    itemCount,
                    r.getCreatedAt()
            );
        });
    }

    /**
     * ✅ 승인자 "내 승인함" V2 (UI 개선용)
     * - stepNo / approverRole 포함해서 내려줌 (Inbox 페이지에서 표시)
     */
    @Transactional(readOnly = true)
    public Page<ApprovalInboxItemResponse> myApprovalInboxV2(Role myRole, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<ApprovalStep> steps = approvalStepRepository.findPendingStepsByRole(
                myRole,
                ApprovalStatus.PENDING,
                pageable
        );

        return steps.map(s -> {
            var r = s.getRequest();
            int itemCount = requestItemRepository.findByRequestId(r.getId()).size();

            return new ApprovalInboxItemResponse(
                    r.getId(),              // requestId
                    r.getStatus(),
                    r.getPurpose(),
                    r.getStartDate(),
                    r.getEndDate(),
                    itemCount,
                    r.getCreatedAt(),
                    s.getStepNo(),
                    s.getApproverRole().name()
            );
        });
    }

    /**
     * 내 대여 목록
     */
    @Transactional(readOnly = true)
    public Page<RentalSummaryResponse> myRentals(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return rentalRepository.findByRenterId(userId, pageable)
                .map(rt -> new RentalSummaryResponse(
                        rt.getId(),
                        rt.getStatus(),
                        rt.getRenter().getEmail(),
                        rt.getStartDate(),
                        rt.getEndDate(),
                        rt.getIssuedAt(),
                        rt.getReturnedAt(),
                        rentalItemRepository.findByRentalId(rt.getId()).size()
                ));
    }

    /**
     * ✅ 관리자/공용: 특정 사용자 대여 목록
     * - 내부적으로 myRentals를 재사용
     */
    @Transactional(readOnly = true)
    public Page<RentalSummaryResponse> userRentals(Long userId, int page, int size) {
        return myRentals(userId, page, size);
    }

    /**
     * 대여 상세(아이템 포함)
     */
    @Transactional(readOnly = true)
    public RentalDetailResponse rentalDetail(Long rentalId) {
        Rental rt = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<RentalItemResponse> items = rentalItemRepository.findByRentalId(rentalId).stream()
                .map(i -> new RentalItemResponse(
                        i.getAsset().getId(),
                        i.getAsset().getAssetCode(),
                        i.getAsset().getName(),
                        i.getStatus()
                ))
                .toList();

        return new RentalDetailResponse(
                rt.getId(),
                rt.getStatus(),
                rt.getRenter().getEmail(),
                rt.getStartDate(),
                rt.getEndDate(),
                rt.getIssuedAt(),
                rt.getReturnedAt(),
                items
        );
    }

    /**
     * 관리자용 대여 목록(전체/상태 필터)
     */
    @Transactional(readOnly = true)
    public Page<RentalSummaryResponse> rentals(RentalStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Rental> pageData = (status == null)
                ? rentalRepository.findAll(pageable)
                : rentalRepository.findByStatus(status, pageable);

        return pageData.map(rt -> new RentalSummaryResponse(
                rt.getId(),
                rt.getStatus(),
                rt.getRenter().getEmail(),
                rt.getStartDate(),
                rt.getEndDate(),
                rt.getIssuedAt(),
                rt.getReturnedAt(),
                rentalItemRepository.findByRentalId(rt.getId()).size()
        ));
    }
}