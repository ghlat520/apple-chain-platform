package com.apple.chain.input.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.input.entity.AgriInventory;
import com.apple.chain.input.entity.AgriUsage;
import com.apple.chain.input.mapper.AgriInventoryMapper;
import com.apple.chain.input.mapper.AgriUsageMapper;
import com.apple.chain.input.service.AgriUsageService;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgriUsageServiceImpl extends ServiceImpl<AgriUsageMapper, AgriUsage> implements AgriUsageService {

    private final AgriInventoryMapper agriInventoryMapper;

    @Override
    public IPage<AgriUsage> listUsages(int page, int size, String keyword, String method, Long orchardId) {
        LambdaQueryWrapper<AgriUsage> wrapper = new LambdaQueryWrapper<AgriUsage>()
                .like(StringUtils.hasText(keyword), AgriUsage::getProductName, keyword)
                .eq(StringUtils.hasText(method), AgriUsage::getMethod, method)
                .eq(orchardId != null, AgriUsage::getOrchardId, orchardId)
                .orderByDesc(AgriUsage::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public AgriUsage getDetail(Long id) {
        AgriUsage usage = getById(id);
        if (usage == null) {
            throw new BizException(ResultCode.NOT_FOUND, "使用记录不存在");
        }
        return usage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriUsage createUsage(AgriUsage usage) {
        save(usage);
        // deduct inventory
        if (usage.getProductId() != null && usage.getQuantity() != null) {
            AgriInventory inventory = agriInventoryMapper.selectOne(
                    new LambdaQueryWrapper<AgriInventory>()
                            .eq(AgriInventory::getProductId, usage.getProductId())
                            .last("LIMIT 1"));
            if (inventory != null) {
                BigDecimal current = inventory.getStockQuantity() == null ? BigDecimal.ZERO : inventory.getStockQuantity();
                BigDecimal newStock = current.subtract(usage.getQuantity());
                if (newStock.signum() < 0) {
                    throw new BizException("库存不足，当前库存: " + current + " " + inventory.getUnit());
                }
                inventory.setStockQuantity(newStock);
                inventory.setStatus(calcInventoryStatus(inventory));
                agriInventoryMapper.updateById(inventory);
            }
        }
        return usage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriUsage updateUsage(Long id, AgriUsage usage) {
        getDetail(id);
        usage.setId(id);
        updateById(usage);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUsage(Long id) {
        AgriUsage usage = getDetail(id);
        removeById(id);
        // restore inventory
        if (usage.getProductId() != null && usage.getQuantity() != null) {
            AgriInventory inventory = agriInventoryMapper.selectOne(
                    new LambdaQueryWrapper<AgriInventory>()
                            .eq(AgriInventory::getProductId, usage.getProductId())
                            .last("LIMIT 1"));
            if (inventory != null) {
                BigDecimal current = inventory.getStockQuantity() == null ? BigDecimal.ZERO : inventory.getStockQuantity();
                inventory.setStockQuantity(current.add(usage.getQuantity()));
                inventory.setStatus(calcInventoryStatus(inventory));
                agriInventoryMapper.updateById(inventory);
            }
        }
    }

    private String calcInventoryStatus(AgriInventory inventory) {
        if (inventory.getStockQuantity() == null || inventory.getStockQuantity().signum() <= 0) {
            return "EMPTY";
        }
        if (inventory.getMaxLevel() != null
                && inventory.getStockQuantity().compareTo(inventory.getMaxLevel()) > 0) {
            return "OVERSTOCKED";
        }
        if (inventory.getWarningLevel() != null
                && inventory.getStockQuantity().compareTo(inventory.getWarningLevel()) <= 0) {
            return "LOW";
        }
        return "NORMAL";
    }

    @Override
    public void exportUsages(String keyword, String method, Long orchardId, HttpServletResponse response) {
        LambdaQueryWrapper<AgriUsage> wrapper = new LambdaQueryWrapper<AgriUsage>()
                .like(StringUtils.hasText(keyword), AgriUsage::getProductName, keyword)
                .eq(StringUtils.hasText(method), AgriUsage::getMethod, method)
                .eq(orchardId != null, AgriUsage::getOrchardId, orchardId)
                .orderByDesc(AgriUsage::getCreateTime);
        List<AgriUsage> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("农资使用记录.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("产品名称,批次编码,果园名称,使用量,单位,使用日期,操作人,施用方式,溯源码");
            for (AgriUsage u : list) {
                writer.println(
                        u.getProductName() + "," + u.getBatchCode() + "," + u.getOrchardName() + "," +
                        u.getQuantity() + "," + u.getUnit() + "," + u.getUsageDate() + "," +
                        u.getOperator() + "," + u.getMethod() + "," + u.getTraceCode()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public List<AgriUsage> listByBatchId(Long batchId) {
        return list(new LambdaQueryWrapper<AgriUsage>()
                .eq(AgriUsage::getBatchId, batchId)
                .orderByDesc(AgriUsage::getUsageDate));
    }

    @Override
    public List<AgriUsage> listByTraceCode(String traceCode) {
        return list(new LambdaQueryWrapper<AgriUsage>()
                .eq(AgriUsage::getTraceCode, traceCode)
                .orderByDesc(AgriUsage::getUsageDate));
    }
}
