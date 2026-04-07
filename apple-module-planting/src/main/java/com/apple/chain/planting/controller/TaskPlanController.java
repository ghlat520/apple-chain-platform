package com.apple.chain.planting.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.TaskPlan;
import com.apple.chain.planting.service.TaskPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * M5 AI 作业计划 endpoints.
 */
@Tag(name = "M5 作业计划")
@RestController
@RequestMapping("/api/planting/task-plan")
@RequiredArgsConstructor
public class TaskPlanController {

    private final TaskPlanService taskPlanService;

    @Operation(summary = "为指定果园生成未来 N 个月的作业计划")
    @PostMapping("/generate")
    @RequirePerm("cultivation:write")
    public R<List<TaskPlan>> generate(@RequestParam Long orchardId,
                                       @RequestParam(defaultValue = "1") int months) {
        return R.ok(taskPlanService.generate(orchardId, months));
    }

    @Operation(summary = "查询计划列表")
    @GetMapping("/list")
    @RequirePerm("cultivation:read")
    public R<List<TaskPlan>> list(@RequestParam Long orchardId,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return R.ok(taskPlanService.list(orchardId, from, to));
    }

    @Operation(summary = "标记完成")
    @PostMapping("/{id}/done")
    @RequirePerm("cultivation:write")
    public R<TaskPlan> markDone(@PathVariable Long id,
                                 @RequestParam(required = false) Long actualOperationId) {
        return R.ok(taskPlanService.markDone(id, actualOperationId));
    }

    @Operation(summary = "跳过计划")
    @PostMapping("/{id}/skip")
    @RequirePerm("cultivation:write")
    public R<TaskPlan> skip(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return R.ok(taskPlanService.skip(id, reason));
    }
}
