package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.OrchardMvp;
import com.apple.chain.planting.mapper.OrchardMvpMapper;
import com.apple.chain.planting.service.OrchardMvpService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrchardMvpServiceImpl extends ServiceImpl<OrchardMvpMapper, OrchardMvp> implements OrchardMvpService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<OrchardMvp> listOrchards(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<OrchardMvp> wrapper = new LambdaQueryWrapper<OrchardMvp>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(OrchardMvp::getOrchardName, keyword)
                        .or().like(OrchardMvp::getOrchardCode, keyword)
                        .or().like(OrchardMvp::getVariety, keyword))
                .eq(StringUtils.hasText(status), OrchardMvp::getStatus, status)
                .orderByDesc(OrchardMvp::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public OrchardMvp getOrchardDetail(Long id) {
        OrchardMvp orchard = getById(id);
        if (orchard == null) {
            throw new BizException(ResultCode.NOT_FOUND, "果园不存在");
        }
        return orchard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrchardMvp createOrchard(OrchardMvp orchard) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        orchard.setOrchardCode(String.format("OC%s%04d", prefix, seq));
        if (!StringUtils.hasText(orchard.getStatus())) {
            orchard.setStatus("ACTIVE");
        }
        save(orchard);
        return orchard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrchardMvp updateOrchard(Long id, OrchardMvp orchard) {
        if (getById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "果园不存在");
        }
        orchard.setId(id);
        orchard.setOrchardCode(null);
        updateById(orchard);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrchard(Long id) {
        if (!removeById(id)) {
            throw new BizException(ResultCode.NOT_FOUND, "果园不存在");
        }
    }

    @Override
    public void exportOrchards(String keyword, String status, HttpServletResponse response) {
        LambdaQueryWrapper<OrchardMvp> wrapper = new LambdaQueryWrapper<OrchardMvp>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(OrchardMvp::getOrchardName, keyword)
                        .or().like(OrchardMvp::getVariety, keyword))
                .eq(StringUtils.hasText(status), OrchardMvp::getStatus, status)
                .orderByDesc(OrchardMvp::getCreateTime);
        List<OrchardMvp> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("果园列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("果园编码,果园名称,农户ID,地址,面积(亩),品种,种植年份,状态,创建时间");
            for (OrchardMvp o : list) {
                writer.println(
                        o.getOrchardCode() + "," +
                        q(o.getOrchardName()) + "," +
                        o.getFarmerId() + "," +
                        q(o.getLocation()) + "," +
                        o.getArea() + "," +
                        q(o.getVariety()) + "," +
                        o.getPlantingYear() + "," +
                        o.getStatus() + "," +
                        o.getCreateTime()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }

    private String q(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
