package com.bookmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookmgt.dto.BorrowRequest;
import com.bookmgt.dto.ExtendRequest;
import com.bookmgt.entity.BorrowRecord;

import java.time.LocalDate;
import java.util.List;

public interface BorrowService {
    Page<BorrowRecord> listRecords(int page, int size, String keyword, Integer status, LocalDate startDate, LocalDate endDate);
    List<BorrowRecord> listByUserId(Long userId);
    List<BorrowRecord> listOverdue();
    BorrowRecord getDetail(Long id);
    BorrowRecord borrowBook(BorrowRequest req);
    BorrowRecord returnBook(Long recordId);
    BorrowRecord renewBook(Long recordId);
    BorrowRecord extendDueDate(ExtendRequest req);
    void batchRemind(List<Long> recordIds);
}
