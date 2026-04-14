package com.apple.chain.trade.wxpay.service;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.wxpay.dto.WxPayCreateRequest;
import com.apple.chain.trade.wxpay.entity.WxPayment;
import com.apple.chain.trade.wxpay.enums.WxPaymentStatus;
import com.apple.chain.trade.wxpay.mapper.WxPaymentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 微信支付 Mock 服务 —— 不对接真实微信支付，仅返回假单号/假状态供演示。
 *
 * <p>createPayment: 生成 Mock {@code prepayId} + {@code outTradeNo}，初始状态 PENDING。
 * <p>queryPayment: 若已是终态则直接返回；否则按 90% 概率置 SUCCESS，10% 保持 PENDING（演示轮询）。
 */
@Service
@RequiredArgsConstructor
public class MockWxPayService extends ServiceImpl<WxPaymentMapper, WxPayment> {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final double SUCCESS_PROBABILITY = 0.9d;

    @Transactional(rollbackFor = Exception.class)
    public WxPayment createPayment(WxPayCreateRequest request) {
        if (request.getAmountCents() == null || request.getAmountCents() <= 0) {
            throw new BizException(ResultCode.FAIL, "支付金额必须大于 0");
        }
        WxPayment payment = new WxPayment();
        payment.setOutTradeNo(generateOutTradeNo());
        payment.setOrderId(request.getOrderId());
        payment.setAmountCents(request.getAmountCents());
        payment.setStatus(WxPaymentStatus.PENDING);
        payment.setPayMethod("WECHAT");
        payment.setPrepayId(generatePrepayId());
        payment.setDescription(request.getDescription());
        save(payment);
        return payment;
    }

    public WxPayment getPayment(Long id) {
        WxPayment payment = getById(id);
        if (payment == null) {
            throw new BizException(ResultCode.NOT_FOUND, "支付记录不存在");
        }
        return payment;
    }

    /**
     * Mock 查询：终态幂等；非终态按概率推进为 SUCCESS（演示轮询流程）。
     */
    @Transactional(rollbackFor = Exception.class)
    public WxPayment queryPayment(Long id) {
        WxPayment payment = getPayment(id);
        if (payment.getStatus() != WxPaymentStatus.PENDING) {
            return payment;
        }
        boolean succeed = ThreadLocalRandom.current().nextDouble() < SUCCESS_PROBABILITY;
        if (succeed) {
            payment.setStatus(WxPaymentStatus.SUCCESS);
            payment.setTransactionId(generateTransactionId());
            payment.setPayTime(LocalDateTime.now());
            updateById(payment);
        }
        return payment;
    }

    public IPage<WxPayment> listPayments(int page, int size, String keyword, String status) {
        WxPaymentStatus statusEnum = WxPaymentStatus.fromName(status);
        LambdaQueryWrapper<WxPayment> wrapper = new LambdaQueryWrapper<WxPayment>()
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(WxPayment::getOutTradeNo, keyword)
                        .or().like(WxPayment::getTransactionId, keyword)
                        .or().like(WxPayment::getDescription, keyword))
                .eq(statusEnum != null, WxPayment::getStatus, statusEnum)
                .orderByDesc(WxPayment::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 查询指定日期内状态为 SUCCESS 的支付记录（对账用）。
     */
    public List<WxPayment> listSuccessByDate(LocalDate date) {
        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
        LambdaQueryWrapper<WxPayment> wrapper = new LambdaQueryWrapper<WxPayment>()
                .eq(WxPayment::getStatus, WxPaymentStatus.SUCCESS)
                .between(WxPayment::getPayTime, start, end)
                .orderByAsc(WxPayment::getPayTime);
        return list(wrapper);
    }

    // ---------------- private ----------------

    private String generateOutTradeNo() {
        return "WXPAY" + LocalDateTime.now().format(TS_FMT)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
    }

    private String generatePrepayId() {
        return "wx" + LocalDateTime.now().format(TS_FMT)
                + "mock" + ThreadLocalRandom.current().nextInt(100_000, 999_999);
    }

    private String generateTransactionId() {
        return "42000" + LocalDateTime.now().format(TS_FMT)
                + ThreadLocalRandom.current().nextInt(100_000, 999_999);
    }
}
