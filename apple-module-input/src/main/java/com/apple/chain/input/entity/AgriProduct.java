package com.apple.chain.input.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Agricultural product (农资产品).
 * Table: agri_product
 */
@Getter
@Setter
@TableName("agri_product")
public class AgriProduct extends BaseEntity {

    /** 唯一编码, e.g. AP20250101001 */
    private String productCode;

    /** 产品名称 */
    private String name;

    /** 类型: FERTILIZER/PESTICIDE/SEED/TOOL */
    private String type;

    /** 生产厂家 */
    private String manufacturer;

    /** 规格, e.g. "50kg/袋" */
    private String spec;

    /** 生产批号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 保质期至 */
    private LocalDate expiryDate;

    /** 登记证号/农药登记号 */
    private String registration;

    /** ACTIVE/DISCONTINUED */
    private String status;

    private String remark;
}
