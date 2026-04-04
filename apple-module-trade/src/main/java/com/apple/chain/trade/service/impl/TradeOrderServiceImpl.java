package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.TradeOrder;
import com.apple.chain.trade.mapper.TradeOrderMapper;
import com.apple.chain.trade.service.TradeOrderService;
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

@Service
@RequiredArgsConstructor
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<TradeOrder> listOrders(int page, int size, String keyword, String status, Long farmerId, Long buyerId) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>()
                .like(StringUtils.hasText(keyword), TradeOrder::getOrderNo, keyword)
                .eq(StringUtils.hasText(status), TradeOrder::getOrderStatus, status)
                .eq(farmerId != null, TradeOrder::getFarmerId, farmerId)
                .eq(buyerId != null, TradeOrder::getBuyerId, buyerId)
                .orderByDesc(TradeOrder::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public TradeOrder getOrderDetail(Long id) {
        TradeOrder order = getById(id);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(TradeOrder order) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        order.setOrderNo(String.format("ORD%s%04d", prefix, seq));
        order.setOrderStatus("DRAFT");
        order.setPaymentStatus("PENDING");

        // Calculate total amount
        if (order.getQuantity() != null && order.getUnitPrice() != null) {
            order.setTotalAmount(order.getQuantity().multiply(order.getUnitPrice()));
        } else {
            order.setTotalAmount(BigDecimal.ZERO);
        }

        if (order.getTradeDate() == null) {
            order.setTradeDate(LocalDate.now());
        }
        save(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder updateOrder(Long id, TradeOrder order) {
        if (getById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        order.setId(id);
        order.setOrderNo(null);
        updateById(order);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder updateOrderStatus(Long id, String status) {
        TradeOrder existing = getOrderDetail(id);
        TradeOrder update = new TradeOrder();
        update.setId(id);
        update.setOrderStatus(status);
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder confirmOrder(Long id) {
        return transitionStatus(id, "DRAFT", "CONFIRMED", "只有草稿状态可以确认");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder deliverOrder(Long id) {
        return transitionStatus(id, "CONFIRMED", "DELIVERED", "只有已确认订单可以标记发货");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder completeOrder(Long id) {
        return transitionStatus(id, "DELIVERED", "COMPLETED", "只有已发货订单可以完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder cancelOrder(Long id) {
        TradeOrder order = getOrderDetail(id);
        if ("COMPLETED".equals(order.getOrderStatus()) || "CANCELLED".equals(order.getOrderStatus())) {
            throw new BizException("当前状态不允许取消");
        }
        TradeOrder update = new TradeOrder();
        update.setId(id);
        update.setOrderStatus("CANCELLED");
        updateById(update);
        return getById(id);
    }

    @Override
    public void exportOrders(String keyword, String status, Long farmerId, Long buyerId, HttpServletResponse response) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>()
                .like(StringUtils.hasText(keyword), TradeOrder::getOrderNo, keyword)
                .eq(StringUtils.hasText(status), TradeOrder::getOrderStatus, status)
                .eq(farmerId != null, TradeOrder::getFarmerId, farmerId)
                .eq(buyerId != null, TradeOrder::getBuyerId, buyerId)
                .orderByDesc(TradeOrder::getCreateTime);
        List<TradeOrder> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("交易订单.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("订单号,品种,数量(kg),单价(元),总金额(元),交易日期,付款状态,订单状态");
            for (TradeOrder o : list) {
                writer.println(
                        o.getOrderNo() + "," + o.getVariety() + "," + o.getQuantity() + "," +
                        o.getUnitPrice() + "," + o.getTotalAmount() + "," +
                        o.getTradeDate() + "," + o.getPaymentStatus() + "," + o.getOrderStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void exportOrdersByPayment(String keyword, String status, String paymentStatus, HttpServletResponse response) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>()
                .like(StringUtils.hasText(keyword), TradeOrder::getOrderNo, keyword)
                .eq(StringUtils.hasText(status), TradeOrder::getOrderStatus, status)
                .eq(StringUtils.hasText(paymentStatus), TradeOrder::getPaymentStatus, paymentStatus)
                .orderByDesc(TradeOrder::getCreateTime);
        List<TradeOrder> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("交易订单.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("订单号,品种,数量(kg),单价(元),总金额(元),交易日期,付款状态,订单状态");
            for (TradeOrder o : list) {
                writer.println(
                        o.getOrderNo() + "," + o.getVariety() + "," + o.getQuantity() + "," +
                        o.getUnitPrice() + "," + o.getTotalAmount() + "," +
                        o.getTradeDate() + "," + o.getPaymentStatus() + "," + o.getOrderStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    private TradeOrder transitionStatus(Long id, String expectedFrom, String targetStatus, String errorMsg) {
        TradeOrder order = getOrderDetail(id);
        if (!expectedFrom.equals(order.getOrderStatus())) {
            throw new BizException(errorMsg);
        }
        TradeOrder update = new TradeOrder();
        update.setId(id);
        update.setOrderStatus(targetStatus);
        updateById(update);
        return getById(id);
    }
}
