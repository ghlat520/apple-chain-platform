package com.apple.chain.coldchain.service.impl;

import com.apple.chain.coldchain.entity.PreCoolTask;
import com.apple.chain.coldchain.mapper.PreCoolTaskMapper;
import com.apple.chain.coldchain.service.PreCoolTaskService;
import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PreCoolTaskServiceImpl extends ServiceImpl<PreCoolTaskMapper, PreCoolTask> implements PreCoolTaskService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final BigDecimal DEFAULT_TARGET_TEMP = new BigDecimal("2.0");

    @Override
    public IPage<PreCoolTask> listTasks(int page, int size, Long vehicleId, String status) {
        LambdaQueryWrapper<PreCoolTask> wrapper = new LambdaQueryWrapper<PreCoolTask>()
                .eq(vehicleId != null, PreCoolTask::getVehicleId, vehicleId)
                .eq(StringUtils.hasText(status), PreCoolTask::getStatus, status)
                .orderByDesc(PreCoolTask::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public PreCoolTask getTaskDetail(Long id) {
        PreCoolTask task = getById(id);
        if (task == null) {
            throw new BizException(ResultCode.NOT_FOUND, "预冷任务不存在");
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PreCoolTask createTask(PreCoolTask task) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        task.setTaskNo(String.format("PC%s%04d", prefix, seq));
        task.setStatus("PENDING");
        if (task.getTargetTemp() == null) {
            task.setTargetTemp(DEFAULT_TARGET_TEMP);
        }
        save(task);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PreCoolTask updateTask(Long id, PreCoolTask task) {
        PreCoolTask existing = getTaskDetail(id);
        if ("COMPLETED".equals(existing.getStatus()) || "COOLING".equals(existing.getStatus())) {
            throw new BizException("冷却中或已完成的任务不允许修改");
        }
        task.setId(id);
        task.setTaskNo(null);
        updateById(task);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        PreCoolTask task = getTaskDetail(id);
        if ("COOLING".equals(task.getStatus())) {
            throw new BizException("冷却中的任务不能删除");
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PreCoolTask start(Long id) {
        PreCoolTask task = getTaskDetail(id);
        if (!"PENDING".equals(task.getStatus())) {
            throw new BizException("只有待执行的任务可以开始冷却");
        }
        PreCoolTask update = new PreCoolTask();
        update.setId(id);
        update.setStatus("COOLING");
        update.setStartTime(LocalDateTime.now());
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PreCoolTask complete(Long id) {
        PreCoolTask task = getTaskDetail(id);
        if (!"COOLING".equals(task.getStatus())) {
            throw new BizException("只有冷却中的任务可以完成");
        }
        LocalDateTime now = LocalDateTime.now();
        int durationMinutes = 0;
        if (task.getStartTime() != null) {
            durationMinutes = (int) ChronoUnit.MINUTES.between(task.getStartTime(), now);
        }
        PreCoolTask update = new PreCoolTask();
        update.setId(id);
        update.setStatus("COMPLETED");
        update.setEndTime(now);
        update.setDuration(durationMinutes);
        updateById(update);
        return getById(id);
    }
}
