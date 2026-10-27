package com.apple.chain.trade.wxpay.dto;

import com.apple.chain.trade.wxpay.entity.WxPayment;
import com.apple.chain.trade.wxpay.enums.WxPaymentStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "微信 Mock 支付响应")
public class WxPayResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String outTradeNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 金额（分） */
    private Long amountCents;

    private WxPaymentStatus status;

    private String payMethod;

    /** Mock 预支付会话 ID */
    private String prepayId;

    /** Mock 微信支付单号 */
    private String transactionId;

    /** 供前端渲染二维码占位图的 Mock 链接（演示用） */
    private String qrCodeUrl;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private String description;

    public static WxPayResponse from(WxPayment p) {
        return WxPayResponse.builder()
                .id(p.getId())
                .outTradeNo(p.getOutTradeNo())
                .orderId(p.getOrderId())
                .amountCents(p.getAmountCents())
                .status(p.getStatus())
                .payMethod(p.getPayMethod())
                .prepayId(p.getPrepayId())
                .transactionId(p.getTransactionId())
                .qrCodeUrl("mock://wxpay/qrcode/" + p.getOutTradeNo())
                .payTime(p.getPayTime())
                .createTime(p.getCreateTime())
                .description(p.getDescription())
                .build();
    }
}
