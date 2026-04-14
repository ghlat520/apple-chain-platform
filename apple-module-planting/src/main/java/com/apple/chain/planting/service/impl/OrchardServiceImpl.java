package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.Farmer;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.mapper.FarmerMapper;
import com.apple.chain.planting.mapper.OrchardMapper;
import com.apple.chain.planting.service.OrchardService;
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
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrchardServiceImpl extends ServiceImpl<OrchardMapper, Orchard> implements OrchardService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final FarmerMapper farmerMapper;

    @Override
    public IPage<Orchard> listOrchards(int page, int size, String keyword, String status, Long farmerId) {
        LambdaQueryWrapper<Orchard> wrapper = new LambdaQueryWrapper<Orchard>()
                .like(StringUtils.hasText(keyword), Orchard::getOrchardName, keyword)
                .or(StringUtils.hasText(keyword), w -> w.like(StringUtils.hasText(keyword), Orchard::getOrchardNo, keyword))
                .eq(StringUtils.hasText(status), Orchard::getStatus, status)
                .eq(farmerId != null, Orchard::getFarmerId, farmerId)
                .orderByDesc(Orchard::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Orchard getOrchardDetail(Long id) {
        Orchard orchard = getById(id);
        if (orchard == null) {
            throw new BizException(ResultCode.NOT_FOUND, "果园不存在");
        }
        return orchard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orchard createOrchard(Orchard orchard) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        orchard.setOrchardNo(String.format("ORD%s%04d", prefix, seq));
        if (!StringUtils.hasText(orchard.getStatus())) {
            orchard.setStatus("NORMAL");
        }
        save(orchard);
        return orchard;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orchard updateOrchard(Long id, Orchard orchard) {
        if (getById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "果园不存在");
        }
        orchard.setId(id);
        orchard.setOrchardNo(null);
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
    public void exportOrchards(String keyword, String status, Long farmerId, HttpServletResponse response) {
        LambdaQueryWrapper<Orchard> wrapper = new LambdaQueryWrapper<Orchard>()
                .like(StringUtils.hasText(keyword), Orchard::getOrchardName, keyword)
                .eq(StringUtils.hasText(status), Orchard::getStatus, status)
                .eq(farmerId != null, Orchard::getFarmerId, farmerId)
                .orderByDesc(Orchard::getCreateTime);
        List<Orchard> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("果园列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("果园编号,果园名称,农户ID,面积(亩),品种,地址,树龄(年),状态,创建时间");
            for (Orchard o : list) {
                writer.println(
                        o.getOrchardNo() + "," +
                        q(o.getOrchardName()) + "," +
                        o.getFarmerId() + "," +
                        o.getArea() + "," +
                        q(o.getVariety()) + "," +
                        q(o.getLocation()) + "," +
                        o.getTreeAge() + "," +
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

    @Override
    public List<Orchard> listMyOrchards(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Farmer farmer = farmerMapper.selectOne(
                new LambdaQueryWrapper<Farmer>().eq(Farmer::getUserId, userId));
        if (farmer == null) {
            return Collections.emptyList();
        }
        return list(new LambdaQueryWrapper<Orchard>()
                .eq(Orchard::getFarmerId, farmer.getId())
                .orderByDesc(Orchard::getCreateTime));
    }
}
