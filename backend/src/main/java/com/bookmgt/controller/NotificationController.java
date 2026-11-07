package com.bookmgt.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bookmgt.common.result.Result;
import com.bookmgt.entity.Notification;
import com.bookmgt.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** 获取当前用户的通知列表 */
    @GetMapping
    public Result<List<Notification>> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(notificationService.listByUser(userId));
    }

    /** 获取未读通知数 */
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        int count = notificationService.countUnread(userId);
        return Result.success(Map.of("count", count));
    }

    /** 标记单条已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    /** 全部标记已读 */
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markAllRead(userId);
        return Result.success();
    }
}
