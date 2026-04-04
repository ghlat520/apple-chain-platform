package com.apple.chain.input.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Agricultural input usage record (农资使用记录).
 * Table: agri_usage
 */
@Getter
@Setter
@TableName("agri_usage")
public class AgriUsage extends BaseEntity {

    /** FK agri_product.id */
    private Long productId;

    /** 冗余产品名称 */
    private String productName;

    /** FK cultivation_batch.id */
    private Long batchId;

    /** 冗余批次编码 */
    private String batchCode;

    /** FK cultivation_operation.id, nullable */
    private Long operationId;

    /** FK farm_orchard.id */
    private Long orchardId;

    /** 冗余果园名称 */
    private String orchardName;

    /** 使用量 */
    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 使用日期 */
    private LocalDate usageDate;

    /** 操作人 */
    private String operator;

    /** 施用方式: 撒施/喷洒/滴灌/穴施 */
    private String method;

    /** 溯源码, nullable */
    private String traceCode;

    private String remark;
}
