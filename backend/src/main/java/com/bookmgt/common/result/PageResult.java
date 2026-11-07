package com.bookmgt.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private long total;
    private long page;
    private long pageSize;
    private long pages;
    private List<T> records;

    public PageResult() {}

    public PageResult(long total, long page, long pageSize, List<T> records) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.pages = (long) Math.ceil((double) total / pageSize);
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, long page, long pageSize, List<T> records) {
        return new PageResult<>(total, page, pageSize, records);
    }
}
