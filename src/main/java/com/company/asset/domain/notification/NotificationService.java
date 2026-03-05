package com.company.asset.domain.notification;

import com.company.asset.common.error.BusinessException;
import com.company.asset.common.error.ErrorCode;
import com.company.asset.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void notify(Long userId, NotificationType type, String message, String targetType, Long targetId) {
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .targetType(targetType)
                .targetId(targetId)
                .build());
    }

    @Transactional
    public void markRead(Long notificationId, Long actorUserId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!n.getUserId().equals(actorUserId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        n.markRead();
    }
}