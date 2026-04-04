package com.apple.chain.common.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Common pagination request parameters.
 */
@Getter
@Setter
public class PageParam {

    private int page = 1;
    private int size = 10;
    private String keyword;

    public int getPage() {
        return Math.max(1, page);
    }

    public int getSize() {
        return Math.min(Math.max(1, size), 100);
    }
}
