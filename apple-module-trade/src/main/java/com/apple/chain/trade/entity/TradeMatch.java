package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * M7 撮合候选. Table: td_trade_match (V19).
 */
@Getter
@Setter
@TableName("td_trade_match")
public class TradeMatch extends BaseEntity {

    private Long supplyId;
    private Long demandId;
    private BigDecimal matchScore;
    private LocalDateTime matchTime;
    private Integer status;

    public static final int STATUS_CANDIDATE  = 0;
    public static final int STATUS_CONTACTED  = 1;
    public static final int STATUS_NEGOTIATE  = 2;
    public static final int STATUS_DEALT      = 3;
    public static final int STATUS_REJECTED   = 4;
}
