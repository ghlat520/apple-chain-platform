package com.apple.chain.coldchain.controller;

import com.apple.chain.coldchain.entity.PreCoolTask;
import com.apple.chain.coldchain.service.PreCoolTaskService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "预冷任务")
@RestController
@RequestMapping("/api/coldchain/precool")
@RequiredArgsConstructor
public class PreCoolTaskController {

    private final PreCoolTaskService preCoolTaskService;

    @Operation(summary = "预冷任务列表（分页）")
    @GetMapping
    public R<PageResult<PreCoolTask>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(preCoolTaskService.listTasks(page, size, vehicleId, status)));
    }

    @Operation(summary = "预冷任务详情")
    @GetMapping("/{id}")
    public R<PreCoolTask> detail(@PathVariable Long id) {
        return R.ok(preCoolTaskService.getTaskDetail(id));
    }

    @Operation(summary = "创建预冷任务")
    @PostMapping
    public R<PreCoolTask> create(@RequestBody PreCoolTask task) {
        return R.ok(preCoolTaskService.createTask(task));
    }

    @Operation(summary = "开始冷却 PENDING→COOLING")
    @PutMapping("/{id}/start")
    public R<PreCoolTask> start(@PathVariable Long id) {
        return R.ok(preCoolTaskService.start(id));
    }

    @Operation(summary = "完成冷却 COOLING→COMPLETED")
    @PutMapping("/{id}/complete")
    public R<PreCoolTask> complete(@PathVariable Long id) {
        return R.ok(preCoolTaskService.complete(id));
    }
}
