package com.apple.chain.trade.wxpay.service;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.wxpay.dto.WxRefundCreateRequest;
import com.apple.chain.trade.wxpay.entity.WxPayment;
import com.apple.chain.trade.wxpay.entity.WxRefund;
import com.apple.chain.trade.wxpay.enums.WxPaymentStatus;
import com.apple.chain.trade.wxpay.enums.WxRefundStatus;
import com.apple.chain.trade.wxpay.mapper.WxRefundMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 微信退款 Mock 服务 —— 返回假 refundId，立即置 SUCCESS 并同步原支付记录为 REFUNDED。
 */
@Service
@RequiredArgsConstructor
public class MockWxRefundService extends ServiceImpl<WxRefundMapper, WxRefund> {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MockWxPayService mockWxPayService;

    @Transactional(rollbackFor = Exception.class)
    public WxRefund createRefund(WxRefundCreateRequest request) {
        WxPayment payment = mockWxPayService.getPayment(request.getPaymentId());
        if (payment.getStatus() != WxPaymentStatus.SUCCESS) {
            throw new BizException(ResultCode.FAIL, "仅支付成功状态可退款，当前状态: " + payment.getStatus());
        }
        if (request.getRefundAmountCents() == null || request.getRefundAmountCents() <= 0) {
            throw new BizException(ResultCode.FAIL, "退款金额必须大于 0");
        }
        if (request.getRefundAmountCents() > payment.getAmountCents()) {
            throw new BizException(ResultCode.FAIL, "退款金额不能超过原支付金额");
        }

        WxRefund refund = new WxRefund();
        refund.setOutRefundNo(generateOutRefundNo());
        refund.setPaymentId(payment.getId());
        refund.setRefundAmountCents(request.getRefundAmountCents());
        refund.setReason(request.getReason());
        refund.setStatus(WxRefundStatus.SUCCESS);
        refund.setRefundId(generateRefundId());
        refund.setRefundTime(LocalDateTime.now());
        save(refund);

        // 同步支付记录状态
        payment.setStatus(WxPaymentStatus.REFUNDED);
        mockWxPayService.updateById(payment);

        return refund;
    }

    public WxRefund getRefund(Long id) {
        WxRefund refund = getById(id);
        if (refund == null) {
            throw new BizException(ResultCode.NOT_FOUND, "退款记录不存在");
        }
        return refund;
    }

    public List<WxRefund> listByPaymentId(Long paymentId) {
        return list(new LambdaQueryWrapper<WxRefund>()
                .eq(WxRefund::getPaymentId, paymentId)
                .orderByDesc(WxRefund::getCreateTime));
    }

    public List<WxRefund> listSuccessByDate(LocalDate date) {
        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
        return list(new LambdaQueryWrapper<WxRefund>()
                .eq(WxRefund::getStatus, WxRefundStatus.SUCCESS)
                .between(WxRefund::getRefundTime, start, end)
                .orderByAsc(WxRefund::getRefundTime));
    }

    // ---------------- private ----------------

    private String generateOutRefundNo() {
        return "WXRFD" + LocalDateTime.now().format(TS_FMT)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
    }

    private String generateRefundId() {
        return "50000" + LocalDateTime.now().format(TS_FMT)
                + ThreadLocalRandom.current().nextInt(100_000, 999_999);
    }
}
