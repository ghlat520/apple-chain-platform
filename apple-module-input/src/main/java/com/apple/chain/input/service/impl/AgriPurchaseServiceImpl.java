package com.apple.chain.input.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.input.entity.AgriInventory;
import com.apple.chain.input.entity.AgriPurchase;
import com.apple.chain.input.mapper.AgriInventoryMapper;
import com.apple.chain.input.mapper.AgriPurchaseMapper;
import com.apple.chain.input.service.AgriPurchaseService;
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
public class AgriPurchaseServiceImpl extends ServiceImpl<AgriPurchaseMapper, AgriPurchase> implements AgriPurchaseService {

    private final AgriInventoryMapper agriInventoryMapper;

    @Override
    public IPage<AgriPurchase> listPurchases(int page, int size, String keyword, String status, Long farmerId) {
        LambdaQueryWrapper<AgriPurchase> wrapper = new LambdaQueryWrapper<AgriPurchase>()
                .like(StringUtils.hasText(keyword), AgriPurchase::getPurchaseNo, keyword)
                .eq(StringUtils.hasText(status), AgriPurchase::getStatus, status)
                .eq(farmerId != null, AgriPurchase::getFarmerId, farmerId)
                .orderByDesc(AgriPurchase::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public AgriPurchase getDetail(Long id) {
        AgriPurchase purchase = getById(id);
        if (purchase == null) {
            throw new BizException(ResultCode.NOT_FOUND, "采购记录不存在");
        }
        return purchase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriPurchase createPurchase(AgriPurchase purchase) {
        if (purchase.getTotalAmount() == null
                && purchase.getQuantity() != null && purchase.getUnitPrice() != null) {
            purchase.setTotalAmount(purchase.getQuantity().multiply(purchase.getUnitPrice()));
        }
        purchase.setStatus("PENDING");
        save(purchase);
        return purchase;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriPurchase approvePurchase(Long id) {
        AgriPurchase purchase = getDetail(id);
        if (!"PENDING".equals(purchase.getStatus())) {
            throw new BizException("只有待审核的采购单可以审批");
        }
        purchase.setStatus("APPROVED");
        updateById(purchase);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriPurchase receivePurchase(Long id) {
        AgriPurchase purchase = getDetail(id);
        if (!"APPROVED".equals(purchase.getStatus())) {
            throw new BizException("只有已审批的采购单可以确认收货");
        }
        purchase.setStatus("RECEIVED");
        updateById(purchase);

        // auto-increment inventory
        if (purchase.getProductId() != null && purchase.getQuantity() != null) {
            AgriInventory inventory = agriInventoryMapper.selectOne(
                    new LambdaQueryWrapper<AgriInventory>()
                            .eq(AgriInventory::getProductId, purchase.getProductId())
                            .last("LIMIT 1"));
            if (inventory != null) {
                BigDecimal newStock = inventory.getStockQuantity() == null
                        ? purchase.getQuantity()
                        : inventory.getStockQuantity().add(purchase.getQuantity());
                inventory.setStockQuantity(newStock);
                inventory.setStatus(calculateInventoryStatus(inventory));
                agriInventoryMapper.updateById(inventory);
            }
        }
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriPurchase cancelPurchase(Long id) {
        AgriPurchase purchase = getDetail(id);
        if (!"PENDING".equals(purchase.getStatus())) {
            throw new BizException("只有待审核的采购单可以取消");
        }
        purchase.setStatus("CANCELLED");
        updateById(purchase);
        return getById(id);
    }

    private String calculateInventoryStatus(AgriInventory inventory) {
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
    @Transactional(rollbackFor = Exception.class)
    public AgriPurchase updatePurchase(Long id, AgriPurchase purchase) {
        AgriPurchase existing = getDetail(id);
        if ("CANCELLED".equals(existing.getStatus())) {
            throw new BizException("已取消的采购单不允许修改");
        }
        purchase.setId(id);
        purchase.setPurchaseNo(null);
        updateById(purchase);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchase(Long id) {
        AgriPurchase existing = getDetail(id);
        if ("RECEIVED".equals(existing.getStatus())) {
            throw new BizException("已收货的采购单不能删除");
        }
        removeById(id);
    }

    @Override
    public void exportPurchases(String keyword, String status, Long farmerId, HttpServletResponse response) {
        LambdaQueryWrapper<AgriPurchase> wrapper = new LambdaQueryWrapper<AgriPurchase>()
                .like(StringUtils.hasText(keyword), AgriPurchase::getPurchaseNo, keyword)
                .eq(StringUtils.hasText(status), AgriPurchase::getStatus, status)
                .eq(farmerId != null, AgriPurchase::getFarmerId, farmerId)
                .orderByDesc(AgriPurchase::getCreateTime);
        List<AgriPurchase> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("农资采购.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("采购单号,产品名称,供应商,数量,单位,单价,总金额,采购日期,状态");
            for (AgriPurchase p : list) {
                writer.println(
                        p.getPurchaseNo() + "," + p.getProductName() + "," + p.getSupplierName() + "," +
                        p.getQuantity() + "," + p.getUnit() + "," + p.getUnitPrice() + "," +
                        p.getTotalAmount() + "," + p.getPurchaseDate() + "," + p.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
