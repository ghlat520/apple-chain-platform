package com.apple.chain.trade.wxpay.dto;

import com.apple.chain.trade.wxpay.entity.WxRefund;
import com.apple.chain.trade.wxpay.enums.WxRefundStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "微信 Mock 退款响应")
public class WxRefundResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String outRefundNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long paymentId;

    /** 退款金额（分） */
    private Long refundAmountCents;

    private String reason;

    private WxRefundStatus status;

    /** Mock 微信退款单号 */
    private String refundId;

    private LocalDateTime refundTime;

    private LocalDateTime createTime;

    public static WxRefundResponse from(WxRefund r) {
        return WxRefundResponse.builder()
                .id(r.getId())
                .outRefundNo(r.getOutRefundNo())
                .paymentId(r.getPaymentId())
                .refundAmountCents(r.getRefundAmountCents())
                .reason(r.getReason())
                .status(r.getStatus())
                .refundId(r.getRefundId())
                .refundTime(r.getRefundTime())
                .createTime(r.getCreateTime())
                .build();
    }
}
