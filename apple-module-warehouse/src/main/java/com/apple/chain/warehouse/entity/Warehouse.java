package com.apple.chain.warehouse.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Warehouse entity.
 * Table: wh_warehouse
 */
@Getter
@Setter
@TableName("wh_warehouse")
public class Warehouse extends BaseEntity {

    /** 仓库编码 WH+日期+序号 */
    private String warehouseCode;

    /** 仓库名称 */
    private String name;

    /** 类型: NORMAL/COLD/ATMOSPHERE 普通/冷库/气调库 */
    private String type;

    /** 位置 */
    private String location;

    /** 总容量（吨） */
    private BigDecimal capacity;

    /** 已用容量（吨） */
    private BigDecimal usedCapacity;

    /** 当前温度（℃） */
    private BigDecimal temperature;

    /** 当前湿度（%） */
    private BigDecimal humidity;

    /** 管理员 */
    private String manager;

    /** 联系电话 */
    private String phone;

    /** ACTIVE/MAINTENANCE/FULL/CLOSED */
    private String status;

    /** 备注 */
    private String remark;
}
