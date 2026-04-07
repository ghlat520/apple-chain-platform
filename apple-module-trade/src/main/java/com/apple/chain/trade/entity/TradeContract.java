package com.apple.chain.trade.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("td_trade_contract")
public class TradeContract extends BaseEntity {
    private Long orderId;
    private String contractNo;
    private String templateId;
    private Long partyA;
    private Long partyB;
    private Integer partyASigned;
    private LocalDateTime partyASignTime;
    private Integer partyBSigned;
    private LocalDateTime partyBSignTime;
    private String contractFileUrl;
    private Integer status;

    public static final int STATUS_DRAFT    = 0;
    public static final int STATUS_PENDING  = 1;
    public static final int STATUS_SIGNED   = 2;
    public static final int STATUS_CANCELED = 3;
}
