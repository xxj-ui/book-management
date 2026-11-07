package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.entity.BorrowConfig;
import com.bookmgt.entity.HolidayConfig;
import com.bookmgt.service.ConfigService;
import com.bookmgt.service.HolidayService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/configs")
public class ConfigController {

    private final ConfigService configService;
    private final HolidayService holidayService;

    public ConfigController(ConfigService configService, HolidayService holidayService) {
        this.configService = configService;
        this.holidayService = holidayService;
    }

    // ... existing methods ...

    /** 手动触发假期延期批量更新 */
    @PostMapping("/holiday/batch-extend")
    public Result<String> batchExtend() {
        int count = holidayService.batchUpdateOverdueInHoliday();
        return Result.success("假期延期完成，共更新 " + count + " 条记录");
    }

    @GetMapping("/borrow")
    public Result<List<BorrowConfig>> getBorrowConfigs() {
        return Result.success(configService.getBorrowConfigs());
    }

    @PutMapping("/borrow")
    public Result<Void> updateBorrowConfigs(@RequestBody List<BorrowConfig> configs) {
        configService.updateBorrowConfigs(configs);
        return Result.success();
    }

    @GetMapping("/holiday")
    public Result<List<HolidayConfig>> getHolidayConfigs() {
        return Result.success(configService.getHolidayConfigs());
    }

    @PutMapping("/holiday")
    public Result<Void> updateHolidayConfig(@RequestBody HolidayConfig config) {
        configService.updateHolidayConfig(config);
        return Result.success();
    }
}
