package com.bookmgt.service.impl;

import com.bookmgt.common.constant.RoleConst;
import com.bookmgt.entity.Book;
import com.bookmgt.entity.BookCopy;
import com.bookmgt.entity.BorrowRecord;
import com.bookmgt.entity.Fine;
import com.bookmgt.mapper.BookCopyMapper;
import com.bookmgt.mapper.BookMapper;
import com.bookmgt.mapper.BorrowRecordMapper;
import com.bookmgt.mapper.FineMapper;
import com.bookmgt.service.DashboardService;
import com.bookmgt.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 工作台服务 —— 归还上架完全由数据库驱动
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookCopyMapper bookCopyMapper;
    private final BookMapper bookMapper;
    private final FineMapper fineMapper;
    private final NotificationService notificationService;

    /** 学历映射 */
    private static final Map<Integer, String> EDU_MAP = Map.of(
            1, "高职", 2, "本科", 3, "硕士", 4, "博士"
    );

    public DashboardServiceImpl(BorrowRecordMapper borrowRecordMapper,
                                BookCopyMapper bookCopyMapper,
                                BookMapper bookMapper,
                                FineMapper fineMapper,
                                NotificationService notificationService) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.bookMapper = bookMapper;
        this.fineMapper = fineMapper;
        this.notificationService = notificationService;
    }

    @Override
    public List<Map<String, Object>> getPendingReturns() {
        List<BorrowRecord> records = borrowRecordMapper.selectPendingShelf();
        List<Map<String, Object>> result = new ArrayList<>();

        for (BorrowRecord r : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());

            // 图书信息
            item.put("title", r.getBookTitle());
            item.put("barcode", r.getBarcode());
            item.put("isbn", r.getIsbn());

            // 归还者信息
            item.put("returner", r.getUserName());
            item.put("returnerRole", getEducationLevelName(r.getUserRoleName()));

            // 归还时间
            String returnTime = r.getReturnDate() != null
                    ? r.getReturnDate().toString() + " 00:00"
                    : "";
            item.put("returnTime", returnTime);

            // 架位
            BookCopy copy = bookCopyMapper.selectById(r.getCopyId());
            String shelf = "";
            if (copy != null) {
                shelf = (copy.getShelfFloor() != null ? copy.getShelfFloor() : "")
                        + "-" + (copy.getShelfZone() != null ? copy.getShelfZone() : "")
                        + "-" + (copy.getShelfNo() != null ? copy.getShelfNo() : "");
            }
            item.put("shelf", shelf);

            // 是否新书
            Book book = bookMapper.selectById(copy != null ? copy.getBookId() : null);
            boolean isNew = false;
            if (book != null && book.getPublishYear() != null) {
                try {
                    int year = Integer.parseInt(book.getPublishYear());
                    isNew = year >= LocalDate.now().getYear() - 1;
                } catch (NumberFormatException ignored) {}
            }
            item.put("isNew", isNew);

            // 超期天数
            if (r.getDueDate() != null && r.getReturnDate() != null
                    && r.getReturnDate().isAfter(r.getDueDate())) {
                long days = ChronoUnit.DAYS.between(r.getDueDate(), r.getReturnDate());
                item.put("overdueDays", (int) days);
            } else {
                item.put("overdueDays", null);
            }

            // 前端本地状态
            item.put("_done", false);
            item.put("_selected", false);

            result.add(item);
        }

        return result;
    }

    @Override
    @Transactional
    public void confirmShelf(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) throw new IllegalArgumentException("记录不存在");

        BookCopy copy = bookCopyMapper.selectById(record.getCopyId());
        if (copy == null) throw new IllegalArgumentException("副本不存在");
        if (copy.getStatus() != RoleConst.COPY_PENDING) {
            throw new IllegalArgumentException("该副本不在待上架状态");
        }

        copy.setStatus(RoleConst.COPY_AVAILABLE);
        bookCopyMapper.updateById(copy);

        record.setStatus(RoleConst.BORROW_RETURNED);
        borrowRecordMapper.updateById(record);

        // 管理员确认上架时计算滞纳金
        LocalDate returnDate = record.getReturnDate() != null ? record.getReturnDate() : LocalDate.now();
        if (record.getDueDate() != null && returnDate.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
            BigDecimal fineRate = BigDecimal.valueOf(0.10);
            BigDecimal fineAmount = fineRate.multiply(BigDecimal.valueOf(overdueDays));

            Fine fine = new Fine();
            fine.setRecordId(record.getId());
            fine.setOverdueDays((int) overdueDays);
            fine.setFineAmount(fineAmount);
            fine.setStatus(RoleConst.FINE_UNPAID);
            fineMapper.insert(fine);
        }
    }

    @Override
    @Transactional
    public void markLost(Long recordId) {
        BorrowRecord record = borrowRecordMapper.selectById(recordId);
        if (record == null) throw new IllegalArgumentException("记录不存在");

        BookCopy copy = bookCopyMapper.selectById(record.getCopyId());
        if (copy == null) throw new IllegalArgumentException("副本不存在");

        // 标记为丢失
        copy.setStatus(RoleConst.COPY_LOST);
        bookCopyMapper.updateById(copy);

        // 借阅记录标记为已归还（状态流转结束）
        record.setStatus(RoleConst.BORROW_RETURNED);
        borrowRecordMapper.updateById(record);

        // 生成丢失赔偿的滞纳金记录（按图书原价估算，这里用 50 元作为统一赔偿金）
        java.math.BigDecimal lostFine = java.math.BigDecimal.valueOf(50.00);
        Fine fine = new Fine();
        fine.setRecordId(record.getId());
        fine.setOverdueDays(0);
        fine.setFineAmount(lostFine);
        fine.setStatus(RoleConst.FINE_UNPAID);
        fineMapper.insert(fine);

        // 发送丢失通知
        try {
            BorrowRecord detail = borrowRecordMapper.selectDetailById(recordId);
            if (detail != null) {
                String title = "📕 图书丢失通知";
                String content = "您借阅的《" + (detail.getBookTitle() != null ? detail.getBookTitle() : "图书")
                        + "》已被管理员标记为丢失，赔偿金 ¥" + lostFine + " 已计入您的待缴费清单。";
                notificationService.addNotification(record.getUserId(), 2, title, content);
            }
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public void batchShelf(List<Long> recordIds) {
        for (Long id : recordIds) {
            confirmShelf(id);
        }
    }

    private String getEducationLevelName(String roleName) {
        if (roleName == null) return "";
        try {
            int level = Integer.parseInt(roleName);
            return EDU_MAP.getOrDefault(level, "");
        } catch (NumberFormatException e) {
            return roleName;
        }
    }
}
