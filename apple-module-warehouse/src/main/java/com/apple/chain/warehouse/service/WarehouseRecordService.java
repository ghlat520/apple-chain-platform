package com.apple.chain.warehouse.service;

import com.apple.chain.warehouse.entity.WarehouseRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Warehouse record service interface.
 */
public interface WarehouseRecordService extends IService<WarehouseRecord> {

    IPage<WarehouseRecord> listRecords(int page, int size, String keyword, String recordType, Long warehouseId);

    WarehouseRecord getRecordDetail(Long id);

    WarehouseRecord createRecord(WarehouseRecord record);

    void deleteRecord(Long id);

    void exportRecords(String keyword, String recordType, Long warehouseId, HttpServletResponse response);
}
