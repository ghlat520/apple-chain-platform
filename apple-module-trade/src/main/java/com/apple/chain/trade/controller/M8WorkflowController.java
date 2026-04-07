package com.apple.chain.trade.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.result.R;
import com.apple.chain.trade.entity.TradeContract;
import com.apple.chain.trade.entity.TradeInvoice;
import com.apple.chain.trade.entity.TradePayment;
import com.apple.chain.trade.service.OrderWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * M8 endpoints. Each step is a discrete REST call so the frontend can drive
 * the state machine deterministically (vs hidden background jobs).
 */
@Tag(name = "M8 合同支付开票")
@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class M8WorkflowController {

    private final OrderWorkflowService workflowService;

    @Operation(summary = "M8.1 创建合同")
    @PostMapping("/contract/create")
    @RequirePerm("trade:write")
    public R<TradeContract> createContract(@RequestParam Long orderId,
                                            @RequestParam Long partyA,
                                            @RequestParam Long partyB) {
        return R.ok(workflowService.createContract(orderId, partyA, partyB));
    }

    @Operation(summary = "M8.2 签署合同")
    @PostMapping("/contract/{orderId}/sign")
    @RequirePerm("trade:write")
    public R<TradeContract> signContract(@PathVariable Long orderId, @RequestParam Long signerUid) {
        return R.ok(workflowService.signContract(orderId, signerUid));
    }

    @Operation(summary = "M8.3 创建支付")
    @PostMapping("/payment/create")
    @RequirePerm("trade:write")
    public R<TradePayment> createPayment(@RequestParam Long orderId,
                                          @RequestParam(defaultValue = "WECHAT") String channel) {
        return R.ok(workflowService.createPayment(orderId, channel));
    }

    @Operation(summary = "M8.4 支付成功回调（mock 模式可手动触发）")
    @PostMapping("/payment/callback/wechat")
    public R<TradePayment> wechatCallback(@RequestParam String paymentNo) {
        // Real implementation verifies WeChat signature; mock just trusts the param
        return R.ok(workflowService.confirmPayment(paymentNo));
    }

    @Operation(summary = "M8.5 手动开票（自动回调失败时）")
    @PostMapping("/invoice/issue")
    @RequirePerm("trade:write")
    public R<TradeInvoice> issueInvoice(@RequestParam Long orderId,
                                         @RequestParam String taxPayer,
                                         @RequestParam String taxNo) {
        return R.ok(workflowService.issueInvoice(orderId, taxPayer, taxNo));
    }
}
