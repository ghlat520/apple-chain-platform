package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.TradeOrder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Trade order service interface.
 */
public interface TradeOrderService extends IService<TradeOrder> {

    IPage<TradeOrder> listOrders(int page, int size, String keyword, String status, Long farmerId, Long buyerId);

    TradeOrder getOrderDetail(Long id);

    TradeOrder createOrder(TradeOrder order);

    TradeOrder updateOrder(Long id, TradeOrder order);

    TradeOrder updateOrderStatus(Long id, String status);

    TradeOrder confirmOrder(Long id);

    TradeOrder deliverOrder(Long id);

    TradeOrder completeOrder(Long id);

    TradeOrder cancelOrder(Long id);

    void exportOrders(String keyword, String status, Long farmerId, Long buyerId, HttpServletResponse response);

    void exportOrdersByPayment(String keyword, String status, String paymentStatus, HttpServletResponse response);
}
