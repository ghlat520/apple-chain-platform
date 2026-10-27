package com.apple.chain.input.dto;

import com.apple.chain.input.entity.AgriPurchase;
import com.apple.chain.input.entity.AgriSupplier;
import com.apple.chain.input.entity.AgriUsage;
import lombok.Data;

import java.util.List;

/**
 * Traceability chain view object (溯源链路VO).
 * Aggregates usage records, purchase records and supplier info for a given traceCode.
 */
@Data
public class AgriTraceChainVO {

    /** 溯源码 */
    private String traceCode;

    /** 使用记录列表 */
    private List<AgriUsage> usages;

    /** 采购记录列表 */
    private List<AgriPurchase> purchases;

    /** 供应商列表 */
    private List<AgriSupplier> suppliers;
}
