package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.SupplyInfo;
import com.apple.chain.trade.mapper.SupplyInfoMapper;
import com.apple.chain.trade.service.SupplyInfoService;
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

@Service
@RequiredArgsConstructor
public class SupplyInfoServiceImpl extends ServiceImpl<SupplyInfoMapper, SupplyInfo> implements SupplyInfoService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<SupplyInfo> listSupplies(int page, int size, String keyword, String variety, String status, Long farmerId) {
        LambdaQueryWrapper<SupplyInfo> wrapper = new LambdaQueryWrapper<SupplyInfo>()
                .like(StringUtils.hasText(keyword), SupplyInfo::getSupplyNo, keyword)
                .eq(StringUtils.hasText(variety), SupplyInfo::getVariety, variety)
                .eq(StringUtils.hasText(status), SupplyInfo::getStatus, status)
                .eq(farmerId != null, SupplyInfo::getFarmerId, farmerId)
                .orderByDesc(SupplyInfo::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public SupplyInfo getSupplyDetail(Long id) {
        SupplyInfo supply = getById(id);
        if (supply == null) {
            throw new BizException(ResultCode.NOT_FOUND, "供货信息不存在");
        }
        return supply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyInfo createSupply(SupplyInfo supply) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        supply.setSupplyNo(String.format("SUP%s%04d", prefix, seq));
        supply.setStatus("DRAFT");
        save(supply);
        return supply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyInfo updateSupply(Long id, SupplyInfo supply) {
        SupplyInfo existing = getSupplyDetail(id);
        if ("MATCHED".equals(existing.getStatus()) || "CLOSED".equals(existing.getStatus())) {
            throw new BizException("当前状态不允许修改");
        }
        supply.setId(id);
        supply.setSupplyNo(null);
        updateById(supply);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplyInfo publishSupply(Long id) {
        SupplyInfo supply = getSupplyDetail(id);
        if (!"DRAFT".equals(supply.getStatus())) {
            throw new BizException("只有草稿状态可以发布");
        }
        SupplyInfo update = new SupplyInfo();
        update.setId(id);
        update.setStatus("PUBLISHED");
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSupply(Long id) {
        SupplyInfo supply = getSupplyDetail(id);
        if ("MATCHED".equals(supply.getStatus())) {
            throw new BizException("已匹配的供货不能删除");
        }
        removeById(id);
    }

    @Override
    public void exportSupplies(String keyword, String variety, String status, Long farmerId, HttpServletResponse response) {
        LambdaQueryWrapper<SupplyInfo> wrapper = new LambdaQueryWrapper<SupplyInfo>()
                .like(StringUtils.hasText(keyword), SupplyInfo::getSupplyNo, keyword)
                .eq(StringUtils.hasText(variety), SupplyInfo::getVariety, variety)
                .eq(StringUtils.hasText(status), SupplyInfo::getStatus, status)
                .eq(farmerId != null, SupplyInfo::getFarmerId, farmerId)
                .orderByDesc(SupplyInfo::getCreateTime);
        List<SupplyInfo> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("供货信息.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("供货编号,农户ID,品种,数量(kg),期望价格(元/kg),采收日期,有效期,质量等级,状态");
            for (SupplyInfo s : list) {
                writer.println(
                        s.getSupplyNo() + "," + s.getFarmerId() + "," + s.getVariety() + "," +
                        s.getQuantity() + "," + s.getPriceExpected() + "," +
                        s.getHarvestDate() + "," + s.getValidUntil() + "," +
                        s.getQuality() + "," + s.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
