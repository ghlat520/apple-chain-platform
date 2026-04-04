package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * MVP Trade order entity.
 * Table: trade_order
 *
 * Status: PENDING / CONFIRMED / SHIPPED / COMPLETED / CANCELLED
 * PaymentStatus: UNPAID / PAID
 */
@Getter
@Setter
@TableName("trade_order")
public class TradeOrderMvp extends BaseEntity {

    /** Auto-generated order number: TO+yyyyMMdd+seq */
    private String orderNo;

    private Long buyerId;
    private String buyerName;
    private String buyerPhone;

    private Long orchardId;
    private String orchardName;
    private String batchCode;
    private String variety;

    /** A / B / C */
    private String grade;

    /** Quantity in kg */
    private BigDecimal quantity;

    private BigDecimal unitPrice;
    private BigDecimal totalAmount;

    /** PENDING / CONFIRMED / SHIPPED / COMPLETED / CANCELLED */
    private String status;

    /** UNPAID / PAID */
    private String paymentStatus;

    private String remark;
}
