package com.company.asset.domain.policy;

import com.company.asset.repository.RentalPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalPolicyService {

    private final RentalPolicyRepository rentalPolicyRepository;

    @Transactional(readOnly = true)
    public RentalPolicy getOrCreateDefault() {
        return rentalPolicyRepository.findById(1L)
                .orElseGet(() -> rentalPolicyRepository.save(
                        RentalPolicy.builder()
                                .maxRentalDays(7)
                                .maxActiveRentalsPerUser(2)
                                .maxExtensions(1)
                                .build()
                ));
    }
}
