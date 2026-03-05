package com.company.asset.web.notification;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.domain.notification.NotificationService;
import com.company.asset.domain.notification.NotificationStatus;
import com.company.asset.repository.NotificationRepository;
import com.company.asset.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<?>> myNotifications(@AuthenticationPrincipal CustomUserDetails principal,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getUserId(), pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(@AuthenticationPrincipal CustomUserDetails principal) {
        long cnt = notificationRepository.countByUserIdAndStatus(principal.getUserId(), NotificationStatus.UNREAD);
        return ApiResponse.ok(cnt);
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> read(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails principal) {
        notificationService.markRead(id, principal.getUserId());
        return ApiResponse.ok();
    }
}