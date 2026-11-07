package com.bookmgt.service;

import java.util.List;
import java.util.Map;

/**
 * 工作台/归还上架服务
 */
public interface DashboardService {

    /** 获取待上架的归还记录列表 */
    List<Map<String, Object>> getPendingReturns();

    /** 确认单条上架 */
    void confirmShelf(Long recordId);

    /** 标记丢失 */
    void markLost(Long recordId);

    /** 批量上架 */
    void batchShelf(List<Long> recordIds);
}
