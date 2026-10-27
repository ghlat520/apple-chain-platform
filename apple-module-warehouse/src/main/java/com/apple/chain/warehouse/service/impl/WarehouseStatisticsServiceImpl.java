package com.apple.chain.warehouse.service.impl;

import com.apple.chain.warehouse.dto.WarehouseStatisticsVO;
import com.apple.chain.warehouse.entity.Warehouse;
import com.apple.chain.warehouse.entity.WarehouseRecord;
import com.apple.chain.warehouse.mapper.WarehouseMapper;
import com.apple.chain.warehouse.mapper.WarehouseRecordMapper;
import com.apple.chain.warehouse.service.WarehouseStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseStatisticsServiceImpl implements WarehouseStatisticsService {

    private final WarehouseMapper warehouseMapper;
    private final WarehouseRecordMapper warehouseRecordMapper;

    @Override
    public WarehouseStatisticsVO getSummary() {
        List<Warehouse> warehouses = warehouseMapper.selectList(null);
        WarehouseStatisticsVO vo = new WarehouseStatisticsVO();
        vo.setTotalWarehouses(warehouses.size());

        BigDecimal totalCap = warehouses.stream()
                .map(w -> w.getCapacity() != null ? w.getCapacity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalUsed = warehouses.stream()
                .map(w -> w.getUsedCapacity() != null ? w.getUsedCapacity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        vo.setTotalCapacity(totalCap);
        vo.setTotalUsed(totalUsed);
        vo.setUtilizationRate(totalCap.compareTo(BigDecimal.ZERO) > 0
                ? totalUsed.divide(totalCap, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);

        vo.setWarehousesByType(warehouses.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getType() != null ? w.getType() : "UNKNOWN",
                        Collectors.counting())));
        vo.setWarehousesByStatus(warehouses.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getStatus() != null ? w.getStatus() : "UNKNOWN",
                        Collectors.counting())));
        return vo;
    }

    @Override
    public WarehouseStatisticsVO.TurnoverVO getTurnover(Long warehouseId, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        LambdaQueryWrapper<WarehouseRecord> wrapper = new LambdaQueryWrapper<WarehouseRecord>()
                .eq(warehouseId != null, WarehouseRecord::getWarehouseId, warehouseId)
                .ge(WarehouseRecord::getRecordDate, since);
        List<WarehouseRecord> records = warehouseRecordMapper.selectList(wrapper);

        BigDecimal inbound = records.stream()
                .filter(r -> "INBOUND".equals(r.getRecordType()))
                .map(r -> r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outbound = records.stream()
                .filter(r -> "OUTBOUND".equals(r.getRecordType()))
                .map(r -> r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        WarehouseStatisticsVO.TurnoverVO vo = new WarehouseStatisticsVO.TurnoverVO();
        vo.setWarehouseId(warehouseId);
        vo.setTotalInbound(inbound);
        vo.setTotalOutbound(outbound);
        BigDecimal avgStock = inbound.add(outbound).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        vo.setTurnoverRate(avgStock.compareTo(BigDecimal.ZERO) > 0
                ? outbound.divide(avgStock, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setRecordCount(records.size());
        return vo;
    }

    @Override
    public WarehouseStatisticsVO.LossVO getLoss(Long warehouseId, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        LambdaQueryWrapper<WarehouseRecord> wrapper = new LambdaQueryWrapper<WarehouseRecord>()
                .eq(warehouseId != null, WarehouseRecord::getWarehouseId, warehouseId)
                .ge(WarehouseRecord::getRecordDate, since);
        List<WarehouseRecord> records = warehouseRecordMapper.selectList(wrapper);

        BigDecimal inbound = records.stream()
                .filter(r -> "INBOUND".equals(r.getRecordType()))
                .map(r -> r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal outbound = records.stream()
                .filter(r -> "OUTBOUND".equals(r.getRecordType()))
                .map(r -> r.getQuantity() != null ? r.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        WarehouseStatisticsVO.LossVO vo = new WarehouseStatisticsVO.LossVO();
        vo.setTotalInbound(inbound);
        vo.setTotalOutbound(outbound);
        vo.setDifference(inbound.subtract(outbound));
        vo.setLossRate(inbound.compareTo(BigDecimal.ZERO) > 0
                ? inbound.subtract(outbound).divide(inbound, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
        return vo;
    }
}
