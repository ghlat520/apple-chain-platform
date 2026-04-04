package com.apple.chain.warehouse.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.warehouse.entity.Warehouse;
import com.apple.chain.warehouse.entity.WarehouseRecord;
import com.apple.chain.warehouse.mapper.WarehouseRecordMapper;
import com.apple.chain.warehouse.service.WarehouseRecordService;
import com.apple.chain.warehouse.service.WarehouseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseRecordServiceImpl extends ServiceImpl<WarehouseRecordMapper, WarehouseRecord> implements WarehouseRecordService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final BigDecimal KG_TO_TON = new BigDecimal("1000");

    private final WarehouseService warehouseService;

    @Override
    public IPage<WarehouseRecord> listRecords(int page, int size, String keyword, String recordType, Long warehouseId) {
        LambdaQueryWrapper<WarehouseRecord> wrapper = new LambdaQueryWrapper<WarehouseRecord>()
                .like(StringUtils.hasText(keyword), WarehouseRecord::getRecordNo, keyword)
                .eq(StringUtils.hasText(recordType), WarehouseRecord::getRecordType, recordType)
                .eq(warehouseId != null, WarehouseRecord::getWarehouseId, warehouseId)
                .orderByDesc(WarehouseRecord::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public WarehouseRecord getRecordDetail(Long id) {
        WarehouseRecord record = getById(id);
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND, "出入库记录不存在");
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarehouseRecord createRecord(WarehouseRecord record) {
        // Generate record number
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        record.setRecordNo(String.format("WR%s%04d", prefix, seq));

        // Validate warehouse exists
        Warehouse warehouse = warehouseService.getWarehouseDetail(record.getWarehouseId());
        record.setWarehouseName(warehouse.getName());

        BigDecimal quantityInTons = record.getQuantity().divide(KG_TO_TON, 4, RoundingMode.HALF_UP);
        BigDecimal currentUsed = warehouse.getUsedCapacity() != null ? warehouse.getUsedCapacity() : BigDecimal.ZERO;

        if ("INBOUND".equals(record.getRecordType())) {
            // Check capacity
            BigDecimal newUsed = currentUsed.add(quantityInTons);
            if (warehouse.getCapacity() != null && newUsed.compareTo(warehouse.getCapacity()) > 0) {
                throw new BizException("仓库容量不足，剩余容量: " +
                        warehouse.getCapacity().subtract(currentUsed) + " 吨");
            }
            Warehouse update = new Warehouse();
            update.setId(warehouse.getId());
            update.setUsedCapacity(newUsed);
            warehouseService.updateById(update);
        } else if ("OUTBOUND".equals(record.getRecordType())) {
            // Check stock
            if (currentUsed.compareTo(quantityInTons) < 0) {
                throw new BizException("库存不足，当前库存: " + currentUsed + " 吨");
            }
            Warehouse update = new Warehouse();
            update.setId(warehouse.getId());
            update.setUsedCapacity(currentUsed.subtract(quantityInTons));
            warehouseService.updateById(update);
        } else {
            throw new BizException("无效的记录类型，必须为 INBOUND 或 OUTBOUND");
        }

        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }

        save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        WarehouseRecord record = getRecordDetail(id);
        removeById(id);
    }

    @Override
    public void exportRecords(String keyword, String recordType, Long warehouseId, HttpServletResponse response) {
        LambdaQueryWrapper<WarehouseRecord> wrapper = new LambdaQueryWrapper<WarehouseRecord>()
                .like(StringUtils.hasText(keyword), WarehouseRecord::getRecordNo, keyword)
                .eq(StringUtils.hasText(recordType), WarehouseRecord::getRecordType, recordType)
                .eq(warehouseId != null, WarehouseRecord::getWarehouseId, warehouseId)
                .orderByDesc(WarehouseRecord::getCreateTime);
        List<WarehouseRecord> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("出入库记录.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("记录编号,仓库名称,类型,批次编码,品种,等级,数量(kg),温度,湿度,操作人,操作日期,溯源码");
            for (WarehouseRecord r : list) {
                writer.println(
                        r.getRecordNo() + "," + r.getWarehouseName() + "," + r.getRecordType() + "," +
                        r.getBatchCode() + "," + r.getVariety() + "," + r.getGrade() + "," +
                        r.getQuantity() + "," + r.getTemperature() + "," + r.getHumidity() + "," +
                        r.getOperator() + "," + r.getRecordDate() + "," + r.getTraceCode()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
