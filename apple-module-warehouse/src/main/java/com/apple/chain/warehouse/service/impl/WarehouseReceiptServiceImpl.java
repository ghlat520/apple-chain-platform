package com.apple.chain.warehouse.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.warehouse.entity.Warehouse;
import com.apple.chain.warehouse.entity.WarehouseReceipt;
import com.apple.chain.warehouse.mapper.WarehouseReceiptMapper;
import com.apple.chain.warehouse.service.WarehouseReceiptService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WarehouseReceiptServiceImpl extends ServiceImpl<WarehouseReceiptMapper, WarehouseReceipt> implements WarehouseReceiptService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Set<String> VALID_STATUSES = Set.of("VALID", "PLEDGED", "TRANSFERRED", "CANCELLED");

    private final WarehouseService warehouseService;

    @Override
    public IPage<WarehouseReceipt> listReceipts(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<WarehouseReceipt> wrapper = new LambdaQueryWrapper<WarehouseReceipt>()
                .like(StringUtils.hasText(keyword), WarehouseReceipt::getReceiptNo, keyword)
                .eq(StringUtils.hasText(status), WarehouseReceipt::getStatus, status)
                .orderByDesc(WarehouseReceipt::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public WarehouseReceipt getReceiptDetail(Long id) {
        WarehouseReceipt receipt = getById(id);
        if (receipt == null) {
            throw new BizException(ResultCode.NOT_FOUND, "仓单不存在");
        }
        return receipt;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarehouseReceipt createReceipt(WarehouseReceipt receipt) {
        // Auto-generate receipt number
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        receipt.setReceiptNo(String.format("WR%s%04d", prefix, seq));

        // Validate warehouse exists and set name
        Warehouse warehouse = warehouseService.getWarehouseDetail(receipt.getWarehouseId());
        receipt.setWarehouseName(warehouse.getName());

        // Set initial status
        receipt.setStatus("VALID");

        // Calculate total value if unit value and quantity provided
        if (receipt.getUnitValue() != null && receipt.getQuantity() != null) {
            receipt.setTotalValue(receipt.getUnitValue().multiply(receipt.getQuantity()));
        }

        save(receipt);
        return receipt;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarehouseReceipt updateReceipt(Long id, WarehouseReceipt receipt) {
        WarehouseReceipt existing = getReceiptDetail(id);
        if (!"VALID".equals(existing.getStatus())) {
            throw new BizException("只有有效状态的仓单可以修改");
        }
        receipt.setId(id);
        receipt.setReceiptNo(null);
        receipt.setStatus(null);

        // Recalculate total value if changed
        if (receipt.getUnitValue() != null && receipt.getQuantity() != null) {
            receipt.setTotalValue(receipt.getUnitValue().multiply(receipt.getQuantity()));
        }

        updateById(receipt);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarehouseReceipt changeStatus(Long id, String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BizException("无效的仓单状态: " + status);
        }
        WarehouseReceipt existing = getReceiptDetail(id);

        // State transition validation
        String currentStatus = existing.getStatus();
        if ("CANCELLED".equals(currentStatus)) {
            throw new BizException("已注销的仓单不能变更状态");
        }
        if ("TRANSFERRED".equals(currentStatus)) {
            throw new BizException("已转让的仓单不能变更状态");
        }

        WarehouseReceipt update = new WarehouseReceipt();
        update.setId(id);
        update.setStatus(status);
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReceipt(Long id) {
        WarehouseReceipt receipt = getReceiptDetail(id);
        if ("PLEDGED".equals(receipt.getStatus())) {
            throw new BizException("质押中的仓单不能删除");
        }
        removeById(id);
    }

    @Override
    public void exportReceipts(String keyword, String status, HttpServletResponse response) {
        LambdaQueryWrapper<WarehouseReceipt> wrapper = new LambdaQueryWrapper<WarehouseReceipt>()
                .like(StringUtils.hasText(keyword), WarehouseReceipt::getReceiptNo, keyword)
                .eq(StringUtils.hasText(status), WarehouseReceipt::getStatus, status)
                .orderByDesc(WarehouseReceipt::getCreateTime);
        List<WarehouseReceipt> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("仓单列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("仓单编号,仓库名称,货主姓名,批次编码,品种,等级,数量(kg),单价(元/kg),总估值(元),入库日期,有效期至,溯源码,状态");
            for (WarehouseReceipt r : list) {
                writer.println(
                        r.getReceiptNo() + "," + r.getWarehouseName() + "," + r.getFarmerName() + "," +
                        r.getBatchCode() + "," + r.getVariety() + "," + r.getGrade() + "," +
                        r.getQuantity() + "," + r.getUnitValue() + "," + r.getTotalValue() + "," +
                        r.getInboundDate() + "," + r.getValidUntil() + "," +
                        r.getTraceCode() + "," + r.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
