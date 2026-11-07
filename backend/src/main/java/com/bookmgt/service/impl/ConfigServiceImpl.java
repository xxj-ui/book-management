package com.bookmgt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookmgt.entity.BorrowConfig;
import com.bookmgt.entity.HolidayConfig;
import com.bookmgt.mapper.BorrowConfigMapper;
import com.bookmgt.mapper.HolidayConfigMapper;
import com.bookmgt.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final BorrowConfigMapper borrowConfigMapper;
    private final HolidayConfigMapper holidayConfigMapper;

    public ConfigServiceImpl(BorrowConfigMapper borrowConfigMapper, HolidayConfigMapper holidayConfigMapper) {
        this.borrowConfigMapper = borrowConfigMapper;
        this.holidayConfigMapper = holidayConfigMapper;
    }

    @Override
    public List<BorrowConfig> getBorrowConfigs() {
        return borrowConfigMapper.selectList(null);
    }

    @Override
    @Transactional
    public void updateBorrowConfigs(List<BorrowConfig> configs) {
        for (BorrowConfig config : configs) {
            if (config.getId() != null) {
                borrowConfigMapper.updateById(config);
            } else {
                borrowConfigMapper.insert(config);
            }
        }
    }

    @Override
    public List<HolidayConfig> getHolidayConfigs() {
        return holidayConfigMapper.selectList(null);
    }

    @Override
    public void updateHolidayConfig(HolidayConfig config) {
        if (config.getId() != null) {
            holidayConfigMapper.updateById(config);
        } else {
            holidayConfigMapper.insert(config);
        }
    }
}
