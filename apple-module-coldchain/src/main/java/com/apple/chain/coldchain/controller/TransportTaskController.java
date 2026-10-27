package com.apple.chain.coldchain.controller;

import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.service.TransportTaskService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "运输任务")
@RestController
@RequestMapping("/api/coldchain/tasks")
@RequiredArgsConstructor
public class TransportTaskController {

    private final TransportTaskService transportTaskService;

    @Operation(summary = "运输任务列表（分页）")
    @GetMapping("/list")
    public R<PageResult<TransportTask>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(transportTaskService.listTasks(page, size, keyword, status)));
    }

    @Operation(summary = "运输任务详情")
    @GetMapping("/{id}")
    public R<TransportTask> detail(@PathVariable Long id) {
        return R.ok(transportTaskService.getTaskDetail(id));
    }

    @Operation(summary = "创建运输任务")
    @PostMapping
    public R<TransportTask> create(@RequestBody TransportTask task) {
        return R.ok(transportTaskService.createTask(task));
    }

    @Operation(summary = "更新运输任务")
    @PutMapping("/{id}")
    public R<TransportTask> update(@PathVariable Long id, @RequestBody TransportTask task) {
        return R.ok(transportTaskService.updateTask(id, task));
    }

    @Operation(summary = "发车")
    @PostMapping("/{id}/depart")
    public R<TransportTask> depart(@PathVariable Long id) {
        return R.ok(transportTaskService.depart(id));
    }

    @Operation(summary = "确认送达")
    @PostMapping("/{id}/deliver")
    public R<TransportTask> deliver(@PathVariable Long id) {
        return R.ok(transportTaskService.deliver(id));
    }

    @Operation(summary = "取消任务")
    @PostMapping("/{id}/cancel")
    public R<TransportTask> cancel(@PathVariable Long id) {
        return R.ok(transportTaskService.cancelTask(id));
    }

    @Operation(summary = "删除运输任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        transportTaskService.deleteTask(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出运输任务CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        transportTaskService.exportTasks(keyword, status, response);
    }
}
