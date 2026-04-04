package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.TradeOrderMvp;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * MVP trade order service interface.
 */
public interface TradeOrderMvpService extends IService<TradeOrderMvp> {

    IPage<TradeOrderMvp> listOrders(int page, int size, String keyword, String status, String paymentStatus);

    TradeOrderMvp getOrderDetail(Long id);

    TradeOrderMvp createOrder(TradeOrderMvp order);

    TradeOrderMvp updateOrder(Long id, TradeOrderMvp order);

    TradeOrderMvp updateStatus(Long id, String status);

    void deleteOrder(Long id);

    void exportOrders(String keyword, String status, String paymentStatus, HttpServletResponse response);
}
