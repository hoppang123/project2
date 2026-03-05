package com.company.asset.repository;

import com.company.asset.domain.approval.ApprovalStatus;
import com.company.asset.domain.approval.ApprovalStep;
import com.company.asset.domain.user.Role;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    // 요청의 승인 단계 전체 조회(단계순)
    List<ApprovalStep> findByRequestIdOrderByStepNoAsc(Long requestId);

    // 요청에서 "다음 처리할 단계"(가장 작은 stepNo의 PENDING) 1건
    Optional<ApprovalStep> findFirstByRequestIdAndStatusOrderByStepNoAsc(Long requestId, ApprovalStatus status);

    /**
     * ✅ 승인자 대기함(역할별)
     * - 내 역할(Role)과 일치하는 PENDING step만
     * - 요청(Request)은 APPROVING 상태인 것만
     * - 페이징 가능
     *
     * 주의:
     * - join fetch 를 Page 쿼리에 쓰면 count 쿼리와 충돌할 수 있어, 여기서는 fetch를 안 씀.
     * - 컨트롤러/서비스에서 필요하면 step.getRequest() 접근 시 LAZY 로딩이 걸릴 수 있으니,
     *   서비스 메서드에 @Transactional(readOnly = true)를 꼭 유지해줘.
     */
    @Query("""
        select s from ApprovalStep s
        where s.status = :status
          and s.approverRole = :role
          and s.request.status = com.company.asset.domain.rental.RequestStatus.APPROVING
        """)
    Page<ApprovalStep> findPendingStepsByRole(
            @Param("role") Role role,
            @Param("status") ApprovalStatus status,
            Pageable pageable
    );
}
