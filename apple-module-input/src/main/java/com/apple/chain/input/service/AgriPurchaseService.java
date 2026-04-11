package com.apple.chain.input.service;

import com.apple.chain.input.entity.AgriPurchase;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Agricultural input purchase service interface.
 */
public interface AgriPurchaseService extends IService<AgriPurchase> {

    IPage<AgriPurchase> listPurchases(int page, int size, String keyword, String status, Long farmerId);

    AgriPurchase getDetail(Long id);

    AgriPurchase createPurchase(AgriPurchase purchase);

    AgriPurchase updatePurchase(Long id, AgriPurchase purchase);

    void deletePurchase(Long id);

    void exportPurchases(String keyword, String status, Long farmerId, HttpServletResponse response);

    AgriPurchase approvePurchase(Long id);

    AgriPurchase receivePurchase(Long id);

    AgriPurchase cancelPurchase(Long id);
}
