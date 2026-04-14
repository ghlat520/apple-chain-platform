package com.apple.chain.trade.wxpay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 微信 Mock 对账单响应 —— 按日聚合支付和退款明细。
 * <p>所有金额字段单位为 <b>分</b>，前端展示换算为元。
 */
@Data
@Builder
@Schema(description = "微信 Mock 对账单响应（按日）")
public class WxReconcileResponse {

    /** 对账日期 */
    private LocalDate billDate;

    /** 当日成功支付笔数 */
    private long paymentCount;

    /** 当日支付总金额（分） */
    private long paymentTotalCents;

    /** 当日成功退款笔数 */
    private long refundCount;

    /** 当日退款总金额（分） */
    private long refundTotalCents;

    /** 净收入（分）= 支付总额 − 退款总额 */
    private long netAmountCents;

    /** 支付明细 */
    private List<WxPayResponse> payments;

    /** 退款明细 */
    private List<WxRefundResponse> refunds;
}
