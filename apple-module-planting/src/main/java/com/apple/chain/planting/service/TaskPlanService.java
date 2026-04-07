package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.TaskPlan;

import java.time.LocalDate;
import java.util.List;

/**
 * M5 AI 作业计划服务.
 *
 * v1: rule-engine over a (variety, month, operation) template table.
 * v2 (out of scope): integrate weather API + historical data + LSTM forecast.
 */
public interface TaskPlanService {

    /**
     * Generate plans for the next {@code months} starting from today.
     *
     * @param orchardId target orchard
     * @param months    1..12
     * @return generated plans (already persisted)
     */
    List<TaskPlan> generate(Long orchardId, int months);

    /** Query plans in a date window. */
    List<TaskPlan> list(Long orchardId, LocalDate from, LocalDate to);

    /** Mark a plan as DONE and link to a cultivation_operation row. */
    TaskPlan markDone(Long planId, Long actualOperationId);

    /** Mark a plan as SKIPPED with optional reason. */
    TaskPlan skip(Long planId, String reason);
}
