package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.entity.TaskPlan;
import com.apple.chain.planting.entity.TaskTemplate;
import com.apple.chain.planting.mapper.OrchardMapper;
import com.apple.chain.planting.mapper.TaskPlanMapper;
import com.apple.chain.planting.mapper.TaskTemplateMapper;
import com.apple.chain.planting.service.TaskPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskPlanServiceImpl implements TaskPlanService {

    private static final int MIN_MONTHS = 1;
    private static final int MAX_MONTHS = 12;

    private final TaskTemplateMapper taskTemplateMapper;
    private final TaskPlanMapper taskPlanMapper;
    private final OrchardMapper orchardMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TaskPlan> generate(Long orchardId, int months) {
        if (orchardId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "orchardId 不能为空");
        }
        if (months < MIN_MONTHS || months > MAX_MONTHS) {
            throw new BizException(ResultCode.PARAM_ERROR,
                    "months 必须在 " + MIN_MONTHS + "~" + MAX_MONTHS + " 之间");
        }

        Orchard orchard = orchardMapper.selectById(orchardId);
        if (orchard == null) {
            throw new BizException(ResultCode.NOT_FOUND, "果园不存在: " + orchardId);
        }
        String variety = orchard.getVariety();
        if (variety == null || variety.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "果园品种为空，无法生成计划");
        }

        LocalDate today = LocalDate.now();
        List<TaskPlan> created = new ArrayList<>();

        for (int monthOffset = 0; monthOffset < months; monthOffset++) {
            YearMonth ym = YearMonth.from(today).plusMonths(monthOffset);
            List<TaskTemplate> templates = taskTemplateMapper.findByVarietyAndMonth(variety, ym.getMonthValue());
            for (TaskTemplate t : templates) {
                int dayStart = clampDay(t.getSuggestedDayStart(), ym);
                int dayEnd   = clampDay(t.getSuggestedDayEnd(), ym);
                // Pick the midpoint as the canonical plan date — single row per template
                // (the spec allows expanding to a date range later when weather is wired in)
                int day = (dayStart + dayEnd) / 2;
                LocalDate planDate = ym.atDay(Math.max(1, day));

                // Skip past dates within the current month
                if (planDate.isBefore(today)) {
                    continue;
                }

                TaskPlan plan = new TaskPlan();
                plan.setOrchardId(orchardId);
                plan.setTemplateId(t.getId());
                plan.setOperationType(t.getOperationType());
                plan.setTaskName(t.getTaskName());
                plan.setPlanDate(planDate);
                plan.setPriority(t.getPriority());
                plan.setMaterialSuggestion(t.getMaterialSuggestion());
                plan.setStatus(TaskPlan.STATUS_PENDING);
                plan.setGeneratedBy(TaskPlan.GEN_AUTO);
                taskPlanMapper.insert(plan);
                created.add(plan);
            }
        }
        return created;
    }

    @Override
    public List<TaskPlan> list(Long orchardId, LocalDate from, LocalDate to) {
        if (orchardId == null || from == null || to == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "参数不能为空");
        }
        if (to.isBefore(from)) {
            throw new BizException(ResultCode.PARAM_ERROR, "to 不能早于 from");
        }
        return taskPlanMapper.findInRange(orchardId, from, to);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskPlan markDone(Long planId, Long actualOperationId) {
        TaskPlan plan = taskPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(ResultCode.NOT_FOUND, "计划不存在: " + planId);
        }
        if (TaskPlan.STATUS_DONE.equals(plan.getStatus())) {
            return plan; // idempotent
        }
        plan.setStatus(TaskPlan.STATUS_DONE);
        plan.setActualOperationId(actualOperationId);
        taskPlanMapper.updateById(plan);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskPlan skip(Long planId, String reason) {
        TaskPlan plan = taskPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(ResultCode.NOT_FOUND, "计划不存在: " + planId);
        }
        plan.setStatus(TaskPlan.STATUS_SKIPPED);
        plan.setRemark(reason);
        taskPlanMapper.updateById(plan);
        return plan;
    }

    /** Make sure the suggested day fits inside the actual length of the month. */
    private static int clampDay(Integer day, YearMonth ym) {
        if (day == null) return 15;
        return Math.min(Math.max(day, 1), ym.lengthOfMonth());
    }
}
