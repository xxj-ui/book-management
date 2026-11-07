package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作台 — 归还上架
 *
 * 数据完全由数据库驱动：
 *   - 还书时副本状态变为"待上架"(3)
 *   - 工作台展示所有待上架的归还记录
 *   - 确认上架后副本状态恢复为"在架"(1)
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** 获取待上架的归还记录 */
    @GetMapping("/returns")
    public Result<List<Map<String, Object>>> getReturns() {
        return Result.success(dashboardService.getPendingReturns());
    }

    /** 确认单条上架 */
    @PutMapping("/returns/{id}/shelf")
    public Result<Void> confirmShelf(@PathVariable Long id) {
        dashboardService.confirmShelf(id);
        return Result.successMsg("上架成功");
    }

    /** 标记丢失 */
    @PutMapping("/returns/{id}/lost")
    public Result<Void> markLost(@PathVariable Long id) {
        dashboardService.markLost(id);
        return Result.successMsg("已标记为丢失");
    }

    /** 批量上架 */
    @PostMapping("/returns/batch-shelf")
    public Result<Void> batchShelf(@RequestBody List<Long> ids) {
        dashboardService.batchShelf(ids);
        return Result.successMsg("批量上架成功");
    }
}
