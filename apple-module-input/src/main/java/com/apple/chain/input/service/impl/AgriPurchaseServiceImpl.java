package com.apple.chain.input.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.input.entity.AgriPurchase;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgriPurchaseServiceImpl extends ServiceImpl<AgriPurchaseMapper, AgriPurchase> implements AgriPurchaseService {

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
        purchase.setStatus("PENDING");
        save(purchase);
        return purchase;
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
