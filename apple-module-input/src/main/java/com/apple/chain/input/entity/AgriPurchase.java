package com.apple.chain.input.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Agricultural input purchase record (农资采购记录).
 * Table: agri_purchase
 */
@Getter
@Setter
@TableName("agri_purchase")
public class AgriPurchase extends BaseEntity {

    /** 采购单号 */
    private String purchaseNo;

    /** FK agri_product.id */
    private Long productId;

    /** 冗余产品名称 */
    private String productName;

    /** FK agri_supplier.id */
    private Long supplierId;

    /** 冗余供应商名称 */
    private String supplierName;

    /** 采购数量 */
    private BigDecimal quantity;

    /** 单位: kg/L/袋/瓶 */
    private String unit;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 采购日期 */
    private LocalDate purchaseDate;

    /** 采购人ID */
    private Long farmerId;

    /** PENDING/RECEIVED/CANCELLED */
    private String status;

    private String remark;
}
