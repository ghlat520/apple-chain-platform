package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("td_trade_invoice")
public class TradeInvoice extends BaseEntity {
    private Long orderId;
    private Long paymentId;
    private String invoiceNo;
    private String invoiceCode;
    private String taxPayer;
    private String taxNo;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private String pdfUrl;
    private LocalDateTime issueTime;
    private Integer status;

    public static final int STATUS_APPLYING = 0;
    public static final int STATUS_SUCCESS  = 1;
    public static final int STATUS_FAILED   = 2;
}
