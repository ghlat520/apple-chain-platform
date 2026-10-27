package com.apple.chain.trade.wxpay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "微信 Mock 支付创建请求")
public class WxPayCreateRequest {

    @Schema(description = "关联业务订单 ID", example = "1001")
    @NotNull
    private Long orderId;

    @Schema(description = "支付金额（分），展示端需除以 100 换算为元", example = "12800")
    @NotNull
    @Min(1)
    private Long amountCents;

    @Schema(description = "商品描述", example = "苹果产业链 · 烟台红富士 100kg")
    private String description;
}
