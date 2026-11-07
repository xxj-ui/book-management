package com.bookmgt.scheduled;

import com.bookmgt.common.constant.RoleConst;
import com.bookmgt.entity.BorrowRecord;
import com.bookmgt.mapper.BorrowRecordMapper;
import com.bookmgt.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时任务
 * 每天早上 8:00 自动为超期用户发送催还通知
 */
@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final BorrowRecordMapper borrowRecordMapper;
    private final NotificationService notificationService;

    public ScheduledTasks(BorrowRecordMapper borrowRecordMapper,
                          NotificationService notificationService) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.notificationService = notificationService;
    }

    /** 每天 8:00 执行超期催还 */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void overdueReminder() {
        log.info("===== 开始执行超期催还定时任务 =====");

        // 查询所有超期记录
        List<BorrowRecord> overdueList = borrowRecordMapper.selectByStatus(RoleConst.BORROW_OVERDUE);
        int count = 0;

        for (BorrowRecord r : overdueList) {
            try {
                String title = "⏰ 超期催还通知";
                String content = "您借阅的《" + (r.getBookTitle() != null ? r.getBookTitle() : "图书")
                        + "》已于 " + r.getDueDate() + " 到期，超期 "
                        + getOverdueDays(r) + " 天，请尽快归还。";

                notificationService.addNotification(r.getUserId(), 2, title, content);
                count++;
            } catch (Exception e) {
                log.error("发送催还通知失败, recordId={}", r.getId(), e);
            }
        }

        log.info("===== 超期催还定时任务完成，共发送 {} 条通知 =====", count);
    }

    /** 计算超期天数 */
    private long getOverdueDays(BorrowRecord r) {
        if (r.getDueDate() == null) return 0;
        java.time.LocalDate now = java.time.LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), now);
    }
}
