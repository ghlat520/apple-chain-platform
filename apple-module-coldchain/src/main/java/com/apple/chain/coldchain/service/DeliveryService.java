package com.apple.chain.coldchain.service;

import com.apple.chain.coldchain.entity.Delivery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

public interface DeliveryService extends IService<Delivery> {

    IPage<Delivery> listDeliveries(int page, int size, String keyword, String status);

    Delivery getDeliveryDetail(Long id);

    Delivery createDelivery(Delivery delivery);

    Delivery updateDelivery(Long id, Delivery delivery);

    Delivery sign(Long id, Delivery delivery);

    void deleteDelivery(Long id);

    void exportDeliveries(String keyword, String status, HttpServletResponse response);
}
