package com.apple.chain.input.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.input.entity.AgriSupplier;
import com.apple.chain.input.mapper.AgriSupplierMapper;
import com.apple.chain.input.service.AgriSupplierService;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgriSupplierServiceImpl extends ServiceImpl<AgriSupplierMapper, AgriSupplier> implements AgriSupplierService {

    @Override
    public IPage<AgriSupplier> listSuppliers(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<AgriSupplier> wrapper = new LambdaQueryWrapper<AgriSupplier>()
                .like(StringUtils.hasText(keyword), AgriSupplier::getName, keyword)
                .eq(StringUtils.hasText(status), AgriSupplier::getStatus, status)
                .orderByDesc(AgriSupplier::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public AgriSupplier getDetail(Long id) {
        AgriSupplier supplier = getById(id);
        if (supplier == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农资供应商不存在");
        }
        return supplier;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriSupplier createSupplier(AgriSupplier supplier) {
        supplier.setStatus("PENDING");
        save(supplier);
        return supplier;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriSupplier updateSupplier(Long id, AgriSupplier supplier) {
        getDetail(id);
        supplier.setId(id);
        updateById(supplier);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupplier(Long id) {
        getDetail(id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriSupplier auditSupplier(Long id, String decision, String reason) {
        AgriSupplier supplier = getDetail(id);
        String newStatus;
        switch (decision.toUpperCase()) {
            case "APPROVE" -> newStatus = "APPROVED";
            case "REJECT" -> {
                newStatus = "REJECTED";
                supplier.setRejectReason(reason);
            }
            case "BLACKLIST" -> newStatus = "BLACKLISTED";
            default -> throw new BizException("无效的审核决策: " + decision);
        }
        supplier.setStatus(newStatus);
        supplier.setAuditTime(LocalDateTime.now());
        updateById(supplier);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgriSupplier reinstateSupplier(Long id) {
        AgriSupplier supplier = getDetail(id);
        if (!"REJECTED".equals(supplier.getStatus())) {
            throw new BizException("只有已拒绝的供应商可以重新提交审核");
        }
        supplier.setStatus("PENDING");
        supplier.setRejectReason(null);
        updateById(supplier);
        return getById(id);
    }

    @Override
    public void exportSuppliers(String keyword, String status, HttpServletResponse response) {
        LambdaQueryWrapper<AgriSupplier> wrapper = new LambdaQueryWrapper<AgriSupplier>()
                .like(StringUtils.hasText(keyword), AgriSupplier::getName, keyword)
                .eq(StringUtils.hasText(status), AgriSupplier::getStatus, status)
                .orderByDesc(AgriSupplier::getCreateTime);
        List<AgriSupplier> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("农资供应商.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("供应商编码,名称,联系人,电话,地址,营业执照号,资质证书,信用评分,状态");
            for (AgriSupplier s : list) {
                writer.println(
                        s.getSupplierCode() + "," + s.getName() + "," + s.getContactPerson() + "," +
                        s.getPhone() + "," + s.getAddress() + "," + s.getLicense() + "," +
                        s.getQualification() + "," + s.getCreditScore() + "," + s.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
