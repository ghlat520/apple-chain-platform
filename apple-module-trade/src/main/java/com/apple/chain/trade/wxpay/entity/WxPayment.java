package com.apple.chain.trade.wxpay.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.apple.chain.trade.wxpay.enums.WxPaymentStatus;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 微信支付 Mock — 支付记录。
 *
 * <p>金额单位：{@code amountCents}（分）—— 前端展示时换算为元（除以 100，保留 2 位）。
 */
@Getter
@Setter
@TableName("td_wx_payment")
public class WxPayment extends BaseEntity {

    /** 商户订单号（唯一） */
    private String outTradeNo;

    /** 关联业务订单 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 金额（分） */
    private Long amountCents;

    /** 支付状态 */
    private WxPaymentStatus status;

    /** 支付方式，固定 WECHAT */
    private String payMethod;

    /** 微信预支付交易会话 ID（Mock） */
    private String prepayId;

    /** 微信支付订单号（Mock） */
    private String transactionId;

    /** 支付完成时间 */
    private LocalDateTime payTime;

    /** 商品描述 */
    private String description;
}
