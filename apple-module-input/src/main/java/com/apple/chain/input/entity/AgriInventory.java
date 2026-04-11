package com.apple.chain.input.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Agricultural input inventory (农资库存).
 * Table: agri_inventory
 */
@Getter
@Setter
@TableName("agri_inventory")
public class AgriInventory extends BaseEntity {

    /** FK agri_product.id */
    private Long productId;

    /** 冗余产品名称 */
    private String productName;

    /** 所属农户 */
    private Long farmerId;

    /** 当前库存 */
    private BigDecimal stockQuantity;

    /** 单位 */
    private String unit;

    /** 预警阈值 */
    private BigDecimal warningLevel;

    /** 超储上限 */
    private BigDecimal maxLevel;

    /** 存放位置 */
    private String warehouse;

    /** NORMAL/LOW/EMPTY/OVERSTOCKED */
    private String status;

    private String remark;
}
