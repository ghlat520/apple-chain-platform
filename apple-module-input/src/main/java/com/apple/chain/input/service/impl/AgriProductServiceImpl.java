package com.apple.chain.input.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.input.entity.AgriProduct;
import com.apple.chain.input.mapper.AgriProductMapper;
import com.apple.chain.input.service.AgriProductService;
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
public class AgriProductServiceImpl extends ServiceImpl<AgriProductMapper, AgriProduct> implements AgriProductService {

    @Override
    public IPage<AgriProduct> listProducts(int page, int size, String keyword, String type, String status) {
        LambdaQueryWrapper<AgriProduct> wrapper = new LambdaQueryWrapper<AgriProduct>()
                .like(StringUtils.hasText(keyword), AgriProduct::getName, keyword)
                .eq(StringUtils.hasText(type), AgriProduct::getType, type)
                .eq(StringUtils.hasText(status), AgriProduct::getStatus, status)
                .orderByDesc(AgriProduct::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public AgriProduct getDetail(Long id) {
        AgriProduct product = getById(id);
        if (product == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农资产品不存在");
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriProduct createProduct(AgriProduct product) {
        product.setStatus("ACTIVE");
        save(product);
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriProduct updateProduct(Long id, AgriProduct product) {
        getDetail(id);
        product.setId(id);
        updateById(product);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        getDetail(id);
        removeById(id);
    }

    @Override
    public void exportProducts(String keyword, String type, String status, HttpServletResponse response) {
        LambdaQueryWrapper<AgriProduct> wrapper = new LambdaQueryWrapper<AgriProduct>()
                .like(StringUtils.hasText(keyword), AgriProduct::getName, keyword)
                .eq(StringUtils.hasText(type), AgriProduct::getType, type)
                .eq(StringUtils.hasText(status), AgriProduct::getStatus, status)
                .orderByDesc(AgriProduct::getCreateTime);
        List<AgriProduct> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("农资产品.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("产品编码,产品名称,类型,生产厂家,规格,批号,生产日期,保质期至,登记证号,状态");
            for (AgriProduct p : list) {
                writer.println(
                        p.getProductCode() + "," + p.getName() + "," + p.getType() + "," +
                        p.getManufacturer() + "," + p.getSpec() + "," + p.getBatchNo() + "," +
                        p.getProductionDate() + "," + p.getExpiryDate() + "," +
                        p.getRegistration() + "," + p.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
