package com.bookmgt.service;

import java.util.List;

public interface BookCoverService {

    /**
     * 为单本图书搜索并保存封面
     * @param bookId 图书ID
     * @return 是否成功获取封面
     */
    boolean fetchCoverForBook(Long bookId);

    /**
     * 批量搜索所有无封面的图书封面
     * @return 成功获取封面的图书ID列表
     */
    List<Long> fetchAllMissingCovers();
}
