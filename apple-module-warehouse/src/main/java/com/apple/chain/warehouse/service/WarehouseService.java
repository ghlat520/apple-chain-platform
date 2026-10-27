package com.apple.chain.warehouse.service;

import com.apple.chain.warehouse.entity.Warehouse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Warehouse service interface.
 */
public interface WarehouseService extends IService<Warehouse> {

    IPage<Warehouse> listWarehouses(int page, int size, String keyword, String type, String status);

    Warehouse getWarehouseDetail(Long id);

    Warehouse createWarehouse(Warehouse warehouse);

    Warehouse updateWarehouse(Long id, Warehouse warehouse);

    void deleteWarehouse(Long id);

    void exportWarehouses(String keyword, String type, String status, HttpServletResponse response);

    Warehouse changeStatus(Long id, String newStatus);

    List<Warehouse> listAlerts();
}
