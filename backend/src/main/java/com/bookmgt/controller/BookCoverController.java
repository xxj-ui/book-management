package com.bookmgt.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.bookmgt.common.result.Result;
import com.bookmgt.service.BookCoverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookCoverController {

    private static final Logger log = LoggerFactory.getLogger(BookCoverController.class);

    private final BookCoverService bookCoverService;

    public BookCoverController(BookCoverService bookCoverService) {
        this.bookCoverService = bookCoverService;
    }

    /**
     * 为指定图书获取封面（联网搜索 → MinIO → 更新数据库）
     */
    @PostMapping("/{id}/fetch-cover")
    public Result<Void> fetchCover(@PathVariable Long id) {
        try {
            // 需要登录
            StpUtil.checkLogin();
            boolean ok = bookCoverService.fetchCoverForBook(id);
            if (ok) {
                return Result.successMsg("封面获取成功");
            }
            return Result.error("未找到封面或获取失败");
        } catch (Exception e) {
            log.error("获取封面异常, bookId={}", id, e);
            return Result.error("获取封面失败: " + e.getMessage());
        }
    }

    /**
     * 批量为所有无封面的图书获取封面
     */
    @PostMapping("/covers/fetch-all")
    public Result<List<Long>> fetchAllCovers() {
        try {
            // 需要登录
            StpUtil.checkLogin();
            List<Long> ids = bookCoverService.fetchAllMissingCovers();
            return Result.success("批量获取完成，成功 " + ids.size() + " 本", ids);
        } catch (Exception e) {
            log.error("批量获取封面临异常", e);
            return Result.error("批量获取封面失败: " + e.getMessage());
        }
    }

    /**
     * 批量为所有无封面的图书生成封面（GET 版本，方便浏览器/curl触发）
     */
    @GetMapping("/covers/generate")
    public Result<List<Long>> generateAllCovers() {
        try {
            List<Long> ids = bookCoverService.fetchAllMissingCovers();
            return Result.success("批量生成完成，成功 " + ids.size() + " 本", ids);
        } catch (Exception e) {
            log.error("批量生成封面临异常", e);
            return Result.error("批量生成封面失败: " + e.getMessage());
        }
    }
}
