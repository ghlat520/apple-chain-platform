package com.apple.chain.finance.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.finance.entity.Pledge;
import com.apple.chain.finance.mapper.PledgeMapper;
import com.apple.chain.finance.service.PledgeService;
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
public class PledgeServiceImpl extends ServiceImpl<PledgeMapper, Pledge> implements PledgeService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<Pledge> listPledges(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<Pledge> wrapper = new LambdaQueryWrapper<Pledge>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Pledge::getPledgeCode, keyword)
                        .or().like(Pledge::getPledgorName, keyword))
                .eq(StringUtils.hasText(status), Pledge::getStatus, status)
                .orderByDesc(Pledge::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Pledge getPledgeDetail(Long id) {
        Pledge pledge = getById(id);
        if (pledge == null) throw new BizException(ResultCode.NOT_FOUND, "质押记录不存在");
        return pledge;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pledge createPledge(Pledge pledge) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        pledge.setPledgeCode(String.format("PL%s%04d", prefix, seq));
        pledge.setStatus("PENDING");
        save(pledge);
        return pledge;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pledge updatePledge(Long id, Pledge pledge) {
        Pledge existing = getPledgeDetail(id);
        if ("ACTIVE".equals(existing.getStatus())) throw new BizException("生效中的质押不允许修改");
        pledge.setId(id);
        pledge.setPledgeCode(null);
        updateById(pledge);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pledge activate(Long id) {
        Pledge pledge = getPledgeDetail(id);
        if (!"PENDING".equals(pledge.getStatus())) throw new BizException("只有待生效的质押可以激活");
        Pledge update = new Pledge();
        update.setId(id);
        update.setStatus("ACTIVE");
        update.setStartDate(LocalDate.now());
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pledge release(Long id) {
        Pledge pledge = getPledgeDetail(id);
        if (!"ACTIVE".equals(pledge.getStatus())) throw new BizException("只有生效中的质押可以解除");
        Pledge update = new Pledge();
        update.setId(id);
        update.setStatus("RELEASED");
        update.setEndDate(LocalDate.now());
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePledge(Long id) {
        Pledge pledge = getPledgeDetail(id);
        if ("ACTIVE".equals(pledge.getStatus())) throw new BizException("生效中的质押不能删除");
        removeById(id);
    }

    @Override
    public void exportPledges(String keyword, String status, HttpServletResponse response) {
        List<Pledge> list = list(new LambdaQueryWrapper<Pledge>()
                .and(StringUtils.hasText(keyword), w -> w.like(Pledge::getPledgeCode, keyword).or().like(Pledge::getPledgorName, keyword))
                .eq(StringUtils.hasText(status), Pledge::getStatus, status));
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("仓单质押.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("质押编号,仓单编码,出质人,质权人,质押物,数量,单位,评估价值,质押率,可贷金额,起始日,到期日,状态");
            for (Pledge p : list) {
                writer.println(p.getPledgeCode() + "," + p.getReceiptCode() + "," + p.getPledgorName() + "," +
                        p.getPledgeeName() + "," + p.getCommodity() + "," + p.getQuantity() + "," + p.getUnit() + "," +
                        p.getAppraisedValue() + "," + p.getPledgeRate() + "," + p.getLoanAmount() + "," +
                        p.getStartDate() + "," + p.getEndDate() + "," + p.getStatus());
            }
            writer.flush();
        } catch (Exception e) { throw new BizException("导出失败: " + e.getMessage()); }
    }
}
