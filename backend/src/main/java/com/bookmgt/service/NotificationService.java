package com.bookmgt.service;

import com.bookmgt.entity.Notification;

import java.util.List;

public interface NotificationService {
    /** 获取用户的通知列表 */
    List<Notification> listByUser(Long userId);

    /** 获取未读通知数 */
    int countUnread(Long userId);

    /** 创建通知 */
    void addNotification(Long userId, int type, String title, String content);

    /** 标记为已读 */
    void markRead(Long id);

    /** 全部标记已读 */
    void markAllRead(Long userId);
}
