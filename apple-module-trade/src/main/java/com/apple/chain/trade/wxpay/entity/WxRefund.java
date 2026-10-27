package com.apple.chain.trade.wxpay.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.apple.chain.trade.wxpay.enums.WxRefundStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 微信支付 Mock — 退款记录。
 *
 * <p>金额单位：{@code refundAmountCents}（分）。
 */
@Getter
@Setter
@TableName("td_wx_refund")
public class WxRefund extends BaseEntity {

    /** 商户退款单号（唯一） */
    private String outRefundNo;

    /** 关联支付记录 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long paymentId;

    /** 退款金额（分） */
    private Long refundAmountCents;

    /** 退款原因 */
    private String reason;

    /** 退款状态 */
    private WxRefundStatus status;

    /** 微信退款单号（Mock） */
    private String refundId;

    /** 退款完成时间 */
    private LocalDateTime refundTime;
}
