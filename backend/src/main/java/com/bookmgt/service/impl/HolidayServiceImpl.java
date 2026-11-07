package com.bookmgt.service.impl;

import com.bookmgt.entity.BorrowRecord;
import com.bookmgt.entity.HolidayConfig;
import com.bookmgt.mapper.BorrowRecordMapper;
import com.bookmgt.mapper.HolidayConfigMapper;
import com.bookmgt.service.HolidayService;
import com.bookmgt.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 假期自动延期服务
 *
 * 逻辑：如果图书的应还日期落在寒暑假区间内，
 * 则自动延期到「假期结束日后一天 + 额外延期天数」
 */
@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayConfigMapper holidayConfigMapper;
    private final BorrowRecordMapper borrowRecordMapper;
    private final NotificationService notificationService;

    public HolidayServiceImpl(HolidayConfigMapper holidayConfigMapper,
                              BorrowRecordMapper borrowRecordMapper,
                              NotificationService notificationService) {
        this.holidayConfigMapper = holidayConfigMapper;
        this.borrowRecordMapper = borrowRecordMapper;
        this.notificationService = notificationService;
    }

    @Override
    public List<HolidayConfig> getActiveHolidays() {
        return holidayConfigMapper.selectList(null);
    }

    @Override
    public LocalDate adjustDueDate(LocalDate dueDate) {
        List<HolidayConfig> holidays = getActiveHolidays();
        LocalDate adjusted = dueDate;

        for (HolidayConfig h : holidays) {
            LocalDate start = h.getStartDate();
            LocalDate end = h.getEndDate();

            // 如果到期日在假期区间内（含边界）
            if (!adjusted.isBefore(start) && !adjusted.isAfter(end)) {
                // 假期结束日后一天 + 额外延期天数
                LocalDate newDue = end.plusDays(1).plusDays(h.getExtensionDays() != null ? h.getExtensionDays() : 0);
                if (newDue.isAfter(adjusted)) {
                    adjusted = newDue;
                    System.out.println("【假期延期】到期日 " + dueDate + " 落在假期内，延至 " + adjusted);
                }
            }
        }
        return adjusted;
    }

    @Override
    @Transactional
    public int batchUpdateOverdueInHoliday() {
        List<HolidayConfig> holidays = getActiveHolidays();
        if (holidays.isEmpty()) return 0;

        // 查找所有借阅中/超期的记录
        List<BorrowRecord> activeRecords = borrowRecordMapper.selectByStatus(1);
        activeRecords.addAll(borrowRecordMapper.selectByStatus(3));

        int updated = 0;
        for (BorrowRecord r : activeRecords) {
            LocalDate originalDue = r.getDueDate();
            LocalDate adjusted = adjustDueDate(originalDue);
            if (!adjusted.equals(originalDue)) {
                r.setDueDate(adjusted);
                borrowRecordMapper.updateById(r);
                updated++;
                // 发送通知
                try {
                    String title = "📅 假期延期通知";
                    String content = "您的借阅记录 " + r.getBorrowNo() + " 原到期日 "
                            + originalDue + " 因假期自动延至 " + adjusted + "。";
                    notificationService.addNotification(r.getUserId(), 1, title, content);
                } catch (Exception ignored) {}
                System.out.println("【假期延期】借阅记录 " + r.getBorrowNo()
                        + " 到期日从 " + originalDue + " 延至 " + adjusted);
            }
        }
        return updated;
    }
}
