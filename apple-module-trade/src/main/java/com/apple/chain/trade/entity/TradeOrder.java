package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Trade order linking a supply and a purchase need.
 * Table: td_trade_order
 */
@Getter
@Setter
@TableName("td_trade_order")
public class TradeOrder extends BaseEntity {

    /** Auto-generated order number: ORD+yyyyMMdd+seq */
    private String orderNo;

    private Long supplyId;
    private Long needId;
    private Long farmerId;
    private Long buyerId;
    private String variety;

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    private LocalDate tradeDate;
    private LocalDate deliveryDate;

    /** PENDING / PAID / REFUNDED */
    private String paymentStatus;

    /** DRAFT / CONFIRMED / DELIVERED / COMPLETED / CANCELLED */
    private String orderStatus;

    /** MinIO path to uploaded contract file */
    private String contractFile;

    private String remark;
}
