package com.apple.chain.warehouse.service;

import com.apple.chain.warehouse.dto.WarehouseStatisticsVO;

public interface WarehouseStatisticsService {

    WarehouseStatisticsVO getSummary();

    WarehouseStatisticsVO.TurnoverVO getTurnover(Long warehouseId, int days);

    WarehouseStatisticsVO.LossVO getLoss(Long warehouseId, int days);
}
