package com.company.asset.domain.rental;

import com.company.asset.repository.ExtensionRequestRepository;
import com.company.asset.web.extension.dto.ExtensionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExtensionQueryService {

    private final ExtensionRequestRepository extensionRequestRepository;

    @Transactional(readOnly = true)
    public Page<ExtensionResponse> myRequests(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return extensionRequestRepository.findByRequesterId(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ExtensionResponse> pending(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return extensionRequestRepository.findByStatus(ExtensionStatus.PENDING, pageable).map(this::toResponse);
    }

    private ExtensionResponse toResponse(ExtensionRequest er) {
        return new ExtensionResponse(
                er.getId(),
                er.getRental().getId(),
                er.getRequester().getEmail(),
                er.getRequestedEndDate(),
                er.getStatus(),
                er.getReason(),
                er.getAdminNote(),
                er.getCreatedAt(),
                er.getActedAt()
        );
    }
}
