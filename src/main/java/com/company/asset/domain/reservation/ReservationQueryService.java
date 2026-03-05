package com.company.asset.domain.reservation;

import com.company.asset.repository.AssetReservationRepository;
import com.company.asset.web.reservation.dto.ReservationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationQueryService {

    private final AssetReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public Page<ReservationSummaryResponse> myReservations(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        return reservationRepository.findByReserverId(userId, pageable)
                .map(r -> new ReservationSummaryResponse(
                        r.getId(),
                        r.getAsset().getId(),
                        r.getAsset().getAssetCode(),
                        r.getAsset().getName(),
                        r.getStartDate(),
                        r.getEndDate(),
                        r.getStatus(),
                        r.getCreatedAt()
                ));
    }
}