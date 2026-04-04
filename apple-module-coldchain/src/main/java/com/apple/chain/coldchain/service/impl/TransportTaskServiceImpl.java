package com.apple.chain.coldchain.service.impl;

import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.mapper.TransportTaskMapper;
import com.apple.chain.coldchain.service.TransportTaskService;
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
public class TransportTaskServiceImpl extends ServiceImpl<TransportTaskMapper, TransportTask> implements TransportTaskService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<TransportTask> listTasks(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<TransportTask>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TransportTask::getTaskCode, keyword)
                        .or().like(TransportTask::getCargoDesc, keyword))
                .eq(StringUtils.hasText(status), TransportTask::getStatus, status)
                .orderByDesc(TransportTask::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public TransportTask getTaskDetail(Long id) {
        TransportTask task = getById(id);
        if (task == null) {
            throw new BizException(ResultCode.NOT_FOUND, "运输任务不存在");
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportTask createTask(TransportTask task) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        task.setTaskCode(String.format("TT%s%04d", prefix, seq));
        task.setStatus("PENDING");
        save(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportTask updateTask(Long id, TransportTask task) {
        TransportTask existing = getTaskDetail(id);
        if ("DELIVERED".equals(existing.getStatus())) {
            throw new BizException("已送达的任务不允许修改");
        }
        task.setId(id);
        task.setTaskCode(null);
        updateById(task);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        TransportTask task = getTaskDetail(id);
        if ("IN_TRANSIT".equals(task.getStatus())) {
            throw new BizException("运输中的任务不能删除");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportTask depart(Long id) {
        TransportTask task = getTaskDetail(id);
        if (!"PENDING".equals(task.getStatus())) {
            throw new BizException("只有待发车的任务可以发车");
        }
        TransportTask update = new TransportTask();
        update.setId(id);
        update.setStatus("IN_TRANSIT");
        update.setActualDepart(LocalDateTime.now());
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransportTask deliver(Long id) {
        TransportTask task = getTaskDetail(id);
        if (!"IN_TRANSIT".equals(task.getStatus())) {
            throw new BizException("只有运输中的任务可以确认送达");
        }
        TransportTask update = new TransportTask();
        update.setId(id);
        update.setStatus("DELIVERED");
        update.setActualArrive(LocalDateTime.now());
        updateById(update);
        return getById(id);
    }

    @Override
    public void exportTasks(String keyword, String status, HttpServletResponse response) {
        List<TransportTask> list = list(new LambdaQueryWrapper<TransportTask>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(TransportTask::getTaskCode, keyword)
                        .or().like(TransportTask::getCargoDesc, keyword))
                .eq(StringUtils.hasText(status), TransportTask::getStatus, status)
                .orderByDesc(TransportTask::getCreateTime));
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("运输任务.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("任务编码,出发地,目的地,货物描述,重量(kg),要求温度,计划发车,实际发车,计划到达,实际到达,距离(km),费用,状态");
            for (TransportTask t : list) {
                writer.println(t.getTaskCode() + "," + t.getOrigin() + "," + t.getDestination() + "," +
                        t.getCargoDesc() + "," + t.getCargoWeight() + "," + t.getRequiredTemp() + "," +
                        t.getPlanDepart() + "," + t.getActualDepart() + "," +
                        t.getPlanArrive() + "," + t.getActualArrive() + "," +
                        t.getDistance() + "," + t.getCost() + "," + t.getStatus());
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
