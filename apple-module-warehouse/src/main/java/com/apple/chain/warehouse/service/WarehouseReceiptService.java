package com.apple.chain.warehouse.service;

import com.apple.chain.warehouse.entity.WarehouseReceipt;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Warehouse receipt service interface.
 */
public interface WarehouseReceiptService extends IService<WarehouseReceipt> {

    IPage<WarehouseReceipt> listReceipts(int page, int size, String keyword, String status);

    WarehouseReceipt getReceiptDetail(Long id);

    WarehouseReceipt createReceipt(WarehouseReceipt receipt);

    WarehouseReceipt updateReceipt(Long id, WarehouseReceipt receipt);

    WarehouseReceipt changeStatus(Long id, String status);

    void deleteReceipt(Long id);

    void exportReceipts(String keyword, String status, HttpServletResponse response);
}
