package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.TradeOrderMvp;
import com.apple.chain.trade.mapper.TradeOrderMvpMapper;
import com.apple.chain.trade.service.TradeOrderMvpService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MVP trade order service.
 * Order status: PENDING → CONFIRMED → SHIPPED → COMPLETED (or CANCELLED)
 */
@Service
@RequiredArgsConstructor
public class TradeOrderMvpServiceImpl extends ServiceImpl<TradeOrderMvpMapper, TradeOrderMvp>
        implements TradeOrderMvpService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<TradeOrderMvp> listOrders(int page, int size, String keyword, String status, String paymentStatus) {
        LambdaQueryWrapper<TradeOrderMvp> wrapper = new LambdaQueryWrapper<TradeOrderMvp>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TradeOrderMvp::getOrderNo, keyword)
                        .or().like(TradeOrderMvp::getBuyerName, keyword)
                        .or().like(TradeOrderMvp::getOrchardName, keyword)
                        .or().like(TradeOrderMvp::getVariety, keyword))
                .eq(StringUtils.hasText(status), TradeOrderMvp::getStatus, status)
                .eq(StringUtils.hasText(paymentStatus), TradeOrderMvp::getPaymentStatus, paymentStatus)
                .orderByDesc(TradeOrderMvp::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public TradeOrderMvp getOrderDetail(Long id) {
        TradeOrderMvp order = getById(id);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrderMvp createOrder(TradeOrderMvp order) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        order.setOrderNo(String.format("TO%s%04d", prefix, seq));
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");

        if (order.getQuantity() != null && order.getUnitPrice() != null) {
            order.setTotalAmount(order.getQuantity().multiply(order.getUnitPrice()));
        } else {
            order.setTotalAmount(BigDecimal.ZERO);
        }

        save(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrderMvp updateOrder(Long id, TradeOrderMvp order) {
        if (getById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        order.setId(id);
        order.setOrderNo(null);

        // Recalculate total if price/quantity changed
        if (order.getQuantity() != null && order.getUnitPrice() != null) {
            order.setTotalAmount(order.getQuantity().multiply(order.getUnitPrice()));
        }

        updateById(order);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrderMvp updateStatus(Long id, String status) {
        TradeOrderMvp existing = getOrderDetail(id);
        validateStatusTransition(existing.getStatus(), status);

        TradeOrderMvp update = new TradeOrderMvp();
        update.setId(id);
        update.setStatus(status);

        // Auto mark payment when completed
        if ("COMPLETED".equals(status)) {
            update.setPaymentStatus("PAID");
        }

        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        if (!removeById(id)) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
    }

    @Override
    public void exportOrders(String keyword, String status, String paymentStatus, HttpServletResponse response) {
        LambdaQueryWrapper<TradeOrderMvp> wrapper = new LambdaQueryWrapper<TradeOrderMvp>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TradeOrderMvp::getOrderNo, keyword)
                        .or().like(TradeOrderMvp::getBuyerName, keyword))
                .eq(StringUtils.hasText(status), TradeOrderMvp::getStatus, status)
                .eq(StringUtils.hasText(paymentStatus), TradeOrderMvp::getPaymentStatus, paymentStatus)
                .orderByDesc(TradeOrderMvp::getCreateTime);
        List<TradeOrderMvp> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("收购订单.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("订单号,采购方,手机,果园,批次,品种,等级,数量(kg),单价(元),总金额(元),订单状态,付款状态");
            for (TradeOrderMvp o : list) {
                writer.println(
                        o.getOrderNo() + "," + q(o.getBuyerName()) + "," + o.getBuyerPhone() + "," +
                        q(o.getOrchardName()) + "," + o.getBatchCode() + "," + q(o.getVariety()) + "," +
                        o.getGrade() + "," + o.getQuantity() + "," + o.getUnitPrice() + "," +
                        o.getTotalAmount() + "," + o.getStatus() + "," + o.getPaymentStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    // ---- private helpers ----

    private void validateStatusTransition(String current, String next) {
        boolean valid = switch (current) {
            case "PENDING"   -> "CONFIRMED".equals(next) || "CANCELLED".equals(next);
            case "CONFIRMED" -> "SHIPPED".equals(next)   || "CANCELLED".equals(next);
            case "SHIPPED"   -> "COMPLETED".equals(next);
            default -> false;
        };
        if (!valid) {
            throw new BizException("状态流转不合法: " + current + " → " + next);
        }
    }

    private String q(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
