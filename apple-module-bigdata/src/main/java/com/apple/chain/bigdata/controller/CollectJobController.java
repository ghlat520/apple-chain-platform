package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdCollectJob;
import com.apple.chain.bigdata.entity.BdCollectJobRun;
import com.apple.chain.bigdata.service.BdCollectJobService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Collect job management — CDC / scheduled / manual pipelines.
 */
@Tag(name = "大数据-采集任务")
@RestController
@RequestMapping("/api/bigdata/job")
@RequiredArgsConstructor
public class CollectJobController {

    private final BdCollectJobService service;

    @Operation(summary = "分页查询采集任务")
    @GetMapping
    public R<PageResult<BdCollectJob>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Long sourceId) {
        LambdaQueryWrapper<BdCollectJob> wrapper = new LambdaQueryWrapper<>();
        if (jobType != null) {
            wrapper.eq(BdCollectJob::getJobType, jobType);
        }
        if (sourceId != null) {
            wrapper.eq(BdCollectJob::getSourceId, sourceId);
        }
        wrapper.orderByDesc(BdCollectJob::getId);
        return R.ok(PageResult.of(service.page(new Page<>(page, size), wrapper)));
    }

    @Operation(summary = "获取采集任务详情")
    @GetMapping("/{id}")
    public R<BdCollectJob> get(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    @Operation(summary = "新增采集任务")
    @PostMapping
    public R<BdCollectJob> create(@RequestBody BdCollectJob body) {
        service.save(body);
        return R.ok(body);
    }

    @Operation(summary = "更新采集任务")
    @PutMapping("/{id}")
    public R<BdCollectJob> update(@PathVariable Long id, @RequestBody BdCollectJob body) {
        body.setId(id);
        service.updateById(body);
        return R.ok(body);
    }

    @Operation(summary = "删除采集任务")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "手动触发任务")
    @PostMapping("/{id}/trigger")
    public R<BdCollectJobRun> trigger(@PathVariable Long id,
                                      @RequestParam(defaultValue = "admin") String triggeredBy) {
        return R.ok(service.triggerJob(id, triggeredBy));
    }

    @Operation(summary = "运行历史")
    @GetMapping("/{id}/runs")
    public R<PageResult<BdCollectJobRun>> runs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(PageResult.of(service.listRuns(id, page, size)));
    }
}
