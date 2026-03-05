package com.company.asset.domain.asset;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.domain.user.User;
import com.company.asset.repository.AssetHistoryRepository;
import com.company.asset.repository.AssetRepository;
import com.company.asset.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetAdminService {

    private final AssetRepository assetRepository;
    private final AssetHistoryRepository assetHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void changeStatus(Long assetId, AssetStatus to, Long actorId, String note) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        AssetStatus before = asset.getStatus();
        asset.changeStatus(to);

        assetHistoryRepository.save(AssetHistory.builder()
                .asset(asset)
                .action(AssetAction.STATUS_CHANGE)
                .beforeStatus(before)
                .afterStatus(to)
                .actor(actor)
                .note(note != null ? note : ("관리자 상태 변경: " + before + " → " + to))
                .build());
    }
}