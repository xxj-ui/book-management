package com.bookmgt.controller;

import com.bookmgt.common.result.PageResult;
import com.bookmgt.common.result.Result;
import com.bookmgt.dto.BorrowRequest;
import com.bookmgt.dto.ExtendRequest;
import com.bookmgt.entity.BorrowRecord;
import com.bookmgt.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping
    public Result<PageResult<BorrowRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate sd = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate ed = endDate != null ? LocalDate.parse(endDate) : null;
        var p = borrowService.listRecords(page, size, keyword, status, sd, ed);
        return Result.success(PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords()));
    }

    @GetMapping("/user/{userId}")
    public Result<List<BorrowRecord>> listByUser(@PathVariable Long userId) {
        return Result.success(borrowService.listByUserId(userId));
    }

    @GetMapping("/overdue")
    public Result<List<BorrowRecord>> listOverdue() {
        return Result.success(borrowService.listOverdue());
    }

    @GetMapping("/{id}")
    public Result<BorrowRecord> detail(@PathVariable Long id) {
        BorrowRecord r = borrowService.getDetail(id);
        if (r == null) return Result.error(404, "记录不存在");
        return Result.success(r);
    }

    @PostMapping
    public Result<BorrowRecord> borrow(@Valid @RequestBody BorrowRequest req) {
        return Result.success(borrowService.borrowBook(req));
    }

    @PutMapping("/{id}/return")
    public Result<BorrowRecord> returnBook(@PathVariable Long id) {
        return Result.success(borrowService.returnBook(id));
    }

    @PutMapping("/{id}/renew")
    public Result<BorrowRecord> renew(@PathVariable Long id) {
        return Result.success(borrowService.renewBook(id));
    }

    @PutMapping("/extend")
    public Result<BorrowRecord> extend(@Valid @RequestBody ExtendRequest req) {
        return Result.success(borrowService.extendDueDate(req));
    }

    @PostMapping("/batch-remind")
    public Result<Void> batchRemind(@RequestBody List<Long> recordIds) {
        borrowService.batchRemind(recordIds);
        return Result.successMsg("催还通知已发送");
    }
}
