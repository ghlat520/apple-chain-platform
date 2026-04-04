package com.apple.chain.warehouse.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Warehouse inbound/outbound record.
 * Table: wh_record
 */
@Getter
@Setter
@TableName("wh_record")
public class WarehouseRecord extends BaseEntity {

    /** 记录编号 */
    private String recordNo;

    /** FK wh_warehouse.id */
    private Long warehouseId;

    /** 仓库名称（冗余） */
    private String warehouseName;

    /** 类型: INBOUND/OUTBOUND 入库/出库 */
    private String recordType;

    /** 关联种植批次 */
    private String batchCode;

    /** 苹果品种 */
    private String variety;

    /** 质量等级 A/B/C */
    private String grade;

    /** 数量（kg） */
    private BigDecimal quantity;

    /** 操作时温度 */
    private BigDecimal temperature;

    /** 操作时湿度 */
    private BigDecimal humidity;

    /** 操作人 */
    private String operator;

    /** 操作日期 */
    private LocalDate recordDate;

    /** 溯源码 */
    private String traceCode;

    /** 备注 */
    private String remark;
}
