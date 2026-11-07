package com.bookmgt.service;

import com.bookmgt.entity.BorrowConfig;
import com.bookmgt.entity.HolidayConfig;

import java.util.List;

public interface ConfigService {
    List<BorrowConfig> getBorrowConfigs();
    void updateBorrowConfigs(List<BorrowConfig> configs);
    List<HolidayConfig> getHolidayConfigs();
    void updateHolidayConfig(HolidayConfig config);
}
