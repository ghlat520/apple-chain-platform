package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * M7 议价会话. Table: td_trade_negotiation (V19).
 */
@Getter
@Setter
@TableName("td_trade_negotiation")
public class TradeNegotiation extends BaseEntity {

    private Long matchId;
    private Long supplyUserId;
    private Long demandUserId;
    private BigDecimal currentPrice;
    private BigDecimal currentQuantity;
    private Long lastOfferBy;
    private Integer status;

    public static final int STATUS_ONGOING  = 0;
    public static final int STATUS_DEALT    = 1;
    public static final int STATUS_CANCELED = 2;
}
