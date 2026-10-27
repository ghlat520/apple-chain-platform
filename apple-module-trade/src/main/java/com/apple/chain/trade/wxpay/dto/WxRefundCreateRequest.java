package com.apple.chain.trade.wxpay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "微信 Mock 退款创建请求")
public class WxRefundCreateRequest {

    @Schema(description = "原支付记录 ID", example = "2001")
    @NotNull
    private Long paymentId;

    @Schema(description = "退款金额（分），须 ≤ 原支付金额", example = "12800")
    @NotNull
    @Min(1)
    private Long refundAmountCents;

    @Schema(description = "退款原因", example = "果品到货质量不符，协商退款")
    @NotBlank
    private String reason;
}
