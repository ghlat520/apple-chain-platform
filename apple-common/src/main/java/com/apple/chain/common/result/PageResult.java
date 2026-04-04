package com.apple.chain.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.util.List;

/**
 * Paged result container embedded in R.data.
 */
@Getter
public class PageResult<T> {

    private final List<T> records;
    private final long total;
    private final long current;
    private final long size;

    private PageResult(List<T> records, long total, long current, long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        return new PageResult<>(records, total, current, size);
    }

    public static <T> R<PageResult<T>> toR(IPage<T> page) {
        return R.ok(PageResult.of(page));
    }
}
