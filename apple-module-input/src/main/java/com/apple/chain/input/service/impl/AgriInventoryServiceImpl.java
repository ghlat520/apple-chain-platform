package com.apple.chain.input.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.input.entity.AgriInventory;
import com.apple.chain.input.mapper.AgriInventoryMapper;
import com.apple.chain.input.service.AgriInventoryService;
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
public class AgriInventoryServiceImpl extends ServiceImpl<AgriInventoryMapper, AgriInventory> implements AgriInventoryService {

    @Override
    public IPage<AgriInventory> listInventory(int page, int size, String keyword, String status, Long farmerId) {
        LambdaQueryWrapper<AgriInventory> wrapper = new LambdaQueryWrapper<AgriInventory>()
                .like(StringUtils.hasText(keyword), AgriInventory::getProductName, keyword)
                .eq(StringUtils.hasText(status), AgriInventory::getStatus, status)
                .eq(farmerId != null, AgriInventory::getFarmerId, farmerId)
                .orderByDesc(AgriInventory::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public AgriInventory getDetail(Long id) {
        AgriInventory inventory = getById(id);
        if (inventory == null) {
            throw new BizException(ResultCode.NOT_FOUND, "库存记录不存在");
        }
        return inventory;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriInventory createInventory(AgriInventory inventory) {
        inventory.setStatus(calculateStatus(inventory));
        save(inventory);
        return inventory;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriInventory updateInventory(Long id, AgriInventory inventory) {
        getDetail(id);
        inventory.setId(id);
        inventory.setStatus(calculateStatus(inventory));
        updateById(inventory);
        return getById(id);
    }

    @Override
    public List<AgriInventory> listAlerts() {
        return list(new LambdaQueryWrapper<AgriInventory>()
                .in(AgriInventory::getStatus, "LOW", "EMPTY")
                .orderByAsc(AgriInventory::getStockQuantity));
    }

    private String calculateStatus(AgriInventory inventory) {
        if (inventory.getStockQuantity() == null) {
            return "NORMAL";
        }
        if (inventory.getStockQuantity().signum() <= 0) {
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
    @Transactional(rollbackFor = Exception.class)
    public AgriInventory adjustStock(Long id, BigDecimal delta, String reason) {
        AgriInventory inventory = getDetail(id);
        BigDecimal current = inventory.getStockQuantity() == null ? BigDecimal.ZERO : inventory.getStockQuantity();
        BigDecimal newStock = current.add(delta);
        if (newStock.signum() < 0) {
            throw new BizException("调整后库存不能为负数");
        }
        inventory.setStockQuantity(newStock);
        inventory.setStatus(calculateStatus(inventory));
        if (reason != null) {
            inventory.setRemark(reason);
        }
        updateById(inventory);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInventory(Long id) {
        getDetail(id);
        removeById(id);
    }

    @Override
    public void exportInventory(String keyword, String status, Long farmerId, HttpServletResponse response) {
        LambdaQueryWrapper<AgriInventory> wrapper = new LambdaQueryWrapper<AgriInventory>()
                .like(StringUtils.hasText(keyword), AgriInventory::getProductName, keyword)
                .eq(StringUtils.hasText(status), AgriInventory::getStatus, status)
                .eq(farmerId != null, AgriInventory::getFarmerId, farmerId)
                .orderByDesc(AgriInventory::getCreateTime);
        List<AgriInventory> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("农资库存.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("产品名称,农户ID,库存数量,单位,预警阈值,存放位置,状态");
            for (AgriInventory inv : list) {
                writer.println(
                        inv.getProductName() + "," + inv.getFarmerId() + "," +
                        inv.getStockQuantity() + "," + inv.getUnit() + "," +
                        inv.getWarningLevel() + "," + inv.getWarehouse() + "," + inv.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
