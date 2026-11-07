package com.bookmgt.service.impl;

import com.bookmgt.entity.Notification;
import com.bookmgt.mapper.NotificationMapper;
import com.bookmgt.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public List<Notification> listByUser(Long userId) {
        return notificationMapper.selectByUserId(userId);
    }

    @Override
    public int countUnread(Long userId) {
        Integer count = notificationMapper.countUnread(userId);
        return count != null ? count : 0;
    }

    @Override
    public void addNotification(Long userId, int type, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    @Override
    public void markRead(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    @Override
    public void markAllRead(Long userId) {
        List<Notification> list = notificationMapper.selectByUserId(userId);
        for (Notification n : list) {
            if (n.getIsRead() == 0) {
                n.setIsRead(1);
                notificationMapper.updateById(n);
            }
        }
    }
}
