package com.apple.chain.coldchain.service.impl;

import com.apple.chain.coldchain.entity.Delivery;
import com.apple.chain.coldchain.mapper.DeliveryMapper;
import com.apple.chain.coldchain.service.DeliveryService;
import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl extends ServiceImpl<DeliveryMapper, Delivery> implements DeliveryService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<Delivery> listDeliveries(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<Delivery>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Delivery::getDeliveryCode, keyword)
                        .or().like(Delivery::getReceiverName, keyword))
                .eq(StringUtils.hasText(status), Delivery::getStatus, status)
                .orderByDesc(Delivery::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Delivery getDeliveryDetail(Long id) {
        Delivery delivery = getById(id);
        if (delivery == null) {
            throw new BizException(ResultCode.NOT_FOUND, "配送记录不存在");
        }
        return delivery;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Delivery createDelivery(Delivery delivery) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        delivery.setDeliveryCode(String.format("DL%s%04d", prefix, seq));
        delivery.setStatus("PENDING");
        delivery.setQualityCheck("PENDING");
        save(delivery);
        return delivery;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Delivery updateDelivery(Long id, Delivery delivery) {
        Delivery existing = getDeliveryDetail(id);
        if ("SIGNED".equals(existing.getStatus())) {
            throw new BizException("已签收的配送不允许修改");
        }
        delivery.setId(id);
        delivery.setDeliveryCode(null);
        updateById(delivery);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Delivery sign(Long id, Delivery delivery) {
        Delivery existing = getDeliveryDetail(id);
        if ("SIGNED".equals(existing.getStatus())) {
            throw new BizException("该配送已签收");
        }
        Delivery update = new Delivery();
        update.setId(id);
        update.setStatus("SIGNED");
        update.setSignTime(LocalDateTime.now());
        update.setQualityCheck(delivery.getQualityCheck() != null ? delivery.getQualityCheck() : "PASSED");
        update.setQualityRemark(delivery.getQualityRemark());
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDelivery(Long id) {
        Delivery delivery = getDeliveryDetail(id);
        if ("SIGNED".equals(delivery.getStatus())) {
            throw new BizException("已签收的配送不能删除");
        }
        removeById(id);
    }

    @Override
    public void exportDeliveries(String keyword, String status, HttpServletResponse response) {
        List<Delivery> list = list(new LambdaQueryWrapper<Delivery>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Delivery::getDeliveryCode, keyword)
                        .or().like(Delivery::getReceiverName, keyword))
                .eq(StringUtils.hasText(status), Delivery::getStatus, status)
                .orderByDesc(Delivery::getCreateTime));
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("配送记录.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("配送编码,运输任务ID,收货人,电话,地址,配送时间,签收时间,质检状态,状态");
            for (Delivery d : list) {
                writer.println(d.getDeliveryCode() + "," + d.getTaskId() + "," +
                        d.getReceiverName() + "," + d.getReceiverPhone() + "," + d.getReceiverAddr() + "," +
                        d.getDeliveryTime() + "," + d.getSignTime() + "," +
                        d.getQualityCheck() + "," + d.getStatus());
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
