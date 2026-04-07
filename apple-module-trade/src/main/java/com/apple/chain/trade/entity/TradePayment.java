package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("td_trade_payment")
public class TradePayment extends BaseEntity {
    private Long orderId;
    private String paymentNo;
    private String channel;
    private BigDecimal amount;
    private String thirdTradeNo;
    private Integer status;
    private LocalDateTime payTime;
    private LocalDateTime callbackTime;

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAYING  = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_FAILED  = 3;
    public static final int STATUS_REFUND  = 4;
}
