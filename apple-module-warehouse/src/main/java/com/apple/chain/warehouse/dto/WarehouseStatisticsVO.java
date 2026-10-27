package com.apple.chain.warehouse.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class WarehouseStatisticsVO {

    private int totalWarehouses;
    private BigDecimal totalCapacity;
    private BigDecimal totalUsed;
    private BigDecimal utilizationRate;
    private Map<String, Long> warehousesByType;
    private Map<String, Long> warehousesByStatus;

    @Getter
    @Setter
    public static class TurnoverVO {
        private Long warehouseId;
        private BigDecimal totalInbound;
        private BigDecimal totalOutbound;
        private BigDecimal turnoverRate;
        private int recordCount;
    }

    @Getter
    @Setter
    public static class LossVO {
        private BigDecimal totalInbound;
        private BigDecimal totalOutbound;
        private BigDecimal difference;
        private BigDecimal lossRate;
    }
}
