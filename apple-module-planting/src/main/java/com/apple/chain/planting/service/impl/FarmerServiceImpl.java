package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.Farmer;
import com.apple.chain.planting.mapper.FarmerMapper;
import com.apple.chain.planting.service.FarmerService;
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
public class FarmerServiceImpl extends ServiceImpl<FarmerMapper, Farmer> implements FarmerService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<Farmer> listFarmers(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<Farmer> wrapper = new LambdaQueryWrapper<Farmer>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Farmer::getName, keyword)
                        .or().like(Farmer::getPhone, keyword)
                        .or().like(Farmer::getFarmerCode, keyword))
                .eq(StringUtils.hasText(status), Farmer::getStatus, status)
                .orderByDesc(Farmer::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Farmer getFarmerDetail(Long id) {
        Farmer farmer = getById(id);
        if (farmer == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农户不存在");
        }
        return farmer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Farmer createFarmer(Farmer farmer) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        farmer.setFarmerCode(String.format("FC%s%04d", prefix, seq));
        if (!StringUtils.hasText(farmer.getStatus())) {
            farmer.setStatus("ACTIVE");
        }
        save(farmer);
        return farmer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Farmer updateFarmer(Long id, Farmer farmer) {
        if (getById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农户不存在");
        }
        farmer.setId(id);
        farmer.setFarmerCode(null);
        updateById(farmer);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFarmer(Long id) {
        if (!removeById(id)) {
            throw new BizException(ResultCode.NOT_FOUND, "农户不存在");
        }
    }

    @Override
    public void exportFarmers(String keyword, String status, HttpServletResponse response) {
        LambdaQueryWrapper<Farmer> wrapper = new LambdaQueryWrapper<Farmer>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Farmer::getName, keyword)
                        .or().like(Farmer::getPhone, keyword))
                .eq(StringUtils.hasText(status), Farmer::getStatus, status)
                .orderByDesc(Farmer::getCreateTime);
        List<Farmer> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("农户列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("农户编码,姓名,手机号,身份证号,地址,注册面积(亩),状态,创建时间");
            for (Farmer f : list) {
                writer.println(
                        f.getFarmerCode() + "," +
                        q(f.getName()) + "," +
                        f.getPhone() + "," +
                        q(f.getIdCard()) + "," +
                        q(f.getAddress()) + "," +
                        f.getRegisteredArea() + "," +
                        f.getStatus() + "," +
                        f.getCreateTime()
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
