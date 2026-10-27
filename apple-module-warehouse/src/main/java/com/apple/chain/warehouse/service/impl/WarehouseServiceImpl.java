package com.apple.chain.warehouse.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.warehouse.entity.Warehouse;
import com.apple.chain.warehouse.mapper.WarehouseMapper;
import com.apple.chain.warehouse.service.WarehouseService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<Warehouse> listWarehouses(int page, int size, String keyword, String type, String status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<Warehouse>()
                .like(StringUtils.hasText(keyword), Warehouse::getName, keyword)
                .eq(StringUtils.hasText(type), Warehouse::getType, type)
                .eq(StringUtils.hasText(status), Warehouse::getStatus, status)
                .orderByDesc(Warehouse::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Warehouse getWarehouseDetail(Long id) {
        Warehouse warehouse = getById(id);
        if (warehouse == null) {
            throw new BizException(ResultCode.NOT_FOUND, "仓库不存在");
        }
        return warehouse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Warehouse createWarehouse(Warehouse warehouse) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        warehouse.setWarehouseCode(String.format("WH%s%04d", prefix, seq));
        warehouse.setUsedCapacity(BigDecimal.ZERO);
        warehouse.setStatus("ACTIVE");
        save(warehouse);
        return warehouse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Warehouse updateWarehouse(Long id, Warehouse warehouse) {
        Warehouse existing = getWarehouseDetail(id);
        if ("CLOSED".equals(existing.getStatus())) {
            throw new BizException("已关闭的仓库不允许修改");
        }
        warehouse.setId(id);
        warehouse.setWarehouseCode(null);
        updateById(warehouse);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = getWarehouseDetail(id);
        if (warehouse.getUsedCapacity() != null && warehouse.getUsedCapacity().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException("仓库尚有库存，不能删除");
        }
        removeById(id);
    }

    private static final Map<String, Set<String>> STATUS_TRANSITIONS = Map.of(
            "ACTIVE", Set.of("MAINTENANCE", "CLOSED", "FULL"),
            "MAINTENANCE", Set.of("ACTIVE"),
            "FULL", Set.of("ACTIVE")
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Warehouse changeStatus(Long id, String newStatus) {
        Warehouse warehouse = getWarehouseDetail(id);
        String current = warehouse.getStatus();
        if ("CLOSED".equals(current)) {
            throw new BizException("已关闭的仓库不能变更状态");
        }
        Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BizException("不允许的状态变更: " + current + " → " + newStatus);
        }
        if ("CLOSED".equals(newStatus) && warehouse.getUsedCapacity() != null
                && warehouse.getUsedCapacity().compareTo(BigDecimal.ZERO) > 0) {
            throw new BizException("仓库尚有库存，不能关闭");
        }
        Warehouse update = new Warehouse();
        update.setId(id);
        update.setStatus(newStatus);
        updateById(update);
        return getById(id);
    }

    @Override
    public List<Warehouse> listAlerts() {
        List<Warehouse> all = list(new LambdaQueryWrapper<Warehouse>()
                .ne(Warehouse::getStatus, "CLOSED"));
        return all.stream().filter(w -> {
            BigDecimal used = w.getUsedCapacity() != null ? w.getUsedCapacity() : BigDecimal.ZERO;
            BigDecimal cap = w.getCapacity() != null ? w.getCapacity() : BigDecimal.ONE;
            boolean nearFull = cap.compareTo(BigDecimal.ZERO) > 0
                    && used.divide(cap, 2, java.math.RoundingMode.HALF_UP).compareTo(new BigDecimal("0.90")) >= 0;
            boolean emptyActive = "ACTIVE".equals(w.getStatus()) && used.compareTo(BigDecimal.ZERO) == 0;
            boolean maintenance = "MAINTENANCE".equals(w.getStatus());
            return nearFull || emptyActive || maintenance;
        }).toList();
    }

    @Override
    public void exportWarehouses(String keyword, String type, String status, HttpServletResponse response) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<Warehouse>()
                .like(StringUtils.hasText(keyword), Warehouse::getName, keyword)
                .eq(StringUtils.hasText(type), Warehouse::getType, type)
                .eq(StringUtils.hasText(status), Warehouse::getStatus, status)
                .orderByDesc(Warehouse::getCreateTime);
        List<Warehouse> list = list(wrapper);

        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("仓库列表.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("仓库编码,名称,类型,位置,总容量(吨),已用容量(吨),温度(℃),湿度(%),管理员,电话,状态");
            for (Warehouse w : list) {
                writer.println(
                        w.getWarehouseCode() + "," + w.getName() + "," + w.getType() + "," +
                        w.getLocation() + "," + w.getCapacity() + "," + w.getUsedCapacity() + "," +
                        w.getTemperature() + "," + w.getHumidity() + "," +
                        w.getManager() + "," + w.getPhone() + "," + w.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
