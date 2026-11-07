package com.bookmgt.service;

import com.bookmgt.entity.HolidayConfig;

import java.time.LocalDate;
import java.util.List;

public interface HolidayService {

    /** 获取当前有效的假期配置 */
    List<HolidayConfig> getActiveHolidays();

    /**
     * 计算实际应还日期：如果到期日落在假期内，自动延期
     * @param dueDate 原始应还日期
     * @return 调整后的应还日期
     */
    LocalDate adjustDueDate(LocalDate dueDate);

    /**
     * 批量更新所有到期日在假期内的借阅记录
     * @return 更新的记录数
     */
    int batchUpdateOverdueInHoliday();
}
