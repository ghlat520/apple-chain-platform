package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.SupplyInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Supply info service interface.
 */
public interface SupplyInfoService extends IService<SupplyInfo> {

    IPage<SupplyInfo> listSupplies(int page, int size, String keyword, String variety, String status, Long farmerId);

    SupplyInfo getSupplyDetail(Long id);

    SupplyInfo createSupply(SupplyInfo supply);

    SupplyInfo updateSupply(Long id, SupplyInfo supply);

    SupplyInfo publishSupply(Long id);

    void deleteSupply(Long id);

    void exportSupplies(String keyword, String variety, String status, Long farmerId, HttpServletResponse response);
}
