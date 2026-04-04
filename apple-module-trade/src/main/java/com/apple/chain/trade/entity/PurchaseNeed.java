package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Purchase need (buyer demand listing).
 * Table: td_purchase_need
 */
@Getter
@Setter
@TableName("td_purchase_need")
public class PurchaseNeed extends BaseEntity {

    private String needNo;
    private Long buyerId;
    private String variety;

    /** Required quantity in kg */
    private BigDecimal quantity;

    /** Maximum acceptable price per kg in yuan */
    private BigDecimal priceMax;

    private LocalDate requireDate;

    /** Quality grade: A / B / C */
    private String quality;

    private String deliveryAddr;
    private String description;

    /** DRAFT / PUBLISHED / MATCHED / CLOSED */
    private String status;
}
