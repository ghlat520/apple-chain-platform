package com.apple.chain.warehouse.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Warehouse receipt (仓单).
 * Table: wh_receipt
 */
@Getter
@Setter
@TableName("wh_receipt")
public class WarehouseReceipt extends BaseEntity {

    /** 仓单编号 WR+日期+序号 */
    private String receiptNo;

    /** FK wh_warehouse.id */
    private Long warehouseId;

    /** 仓库名称（冗余） */
    private String warehouseName;

    /** 货主ID */
    private Long farmerId;

    /** 货主姓名 */
    private String farmerName;

    /** 种植批次编码 */
    private String batchCode;

    /** 苹果品种 */
    private String variety;

    /** 质量等级 */
    private String grade;

    /** 存储数量（kg） */
    private BigDecimal quantity;

    /** 每公斤估值 */
    private BigDecimal unitValue;

    /** 总估值 */
    private BigDecimal totalValue;

    /** 入库日期 */
    private LocalDate inboundDate;

    /** 有效期至 */
    private LocalDate validUntil;

    /** 溯源码 */
    private String traceCode;

    /** VALID/PLEDGED/TRANSFERRED/CANCELLED 有效/质押中/已转让/已注销 */
    private String status;

    /** 备注 */
    private String remark;
}
