package com.apple.chain.input.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.input.entity.AgriUsage;
import com.apple.chain.input.service.AgriUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agricultural input usage endpoints.
 */
@Tag(name = "农资使用记录")
@RestController
@RequestMapping("/api/input/usage")
@RequiredArgsConstructor
public class AgriUsageController {

    private final AgriUsageService agriUsageService;

    @Operation(summary = "使用记录列表（分页）")
    @GetMapping("/list")
    public R<PageResult<AgriUsage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Long orchardId) {
        return R.ok(PageResult.of(agriUsageService.listUsages(page, size, keyword, method, orchardId)));
    }

    @Operation(summary = "使用记录详情")
    @GetMapping("/{id}")
    public R<AgriUsage> detail(@PathVariable Long id) {
        return R.ok(agriUsageService.getDetail(id));
    }

    @Operation(summary = "创建使用记录")
    @PostMapping
    public R<AgriUsage> create(@RequestBody AgriUsage usage) {
        return R.ok(agriUsageService.createUsage(usage));
    }

    @Operation(summary = "更新使用记录")
    @PutMapping("/{id}")
    public R<AgriUsage> update(@PathVariable Long id, @RequestBody AgriUsage usage) {
        return R.ok(agriUsageService.updateUsage(id, usage));
    }

    @Operation(summary = "删除使用记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        agriUsageService.deleteUsage(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出使用记录CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Long orchardId,
            HttpServletResponse response) {
        agriUsageService.exportUsages(keyword, method, orchardId, response);
    }

    @Operation(summary = "按批次查询使用记录")
    @GetMapping("/by-batch")
    public R<List<AgriUsage>> listByBatch(@RequestParam Long batchId) {
        return R.ok(agriUsageService.listByBatchId(batchId));
    }

    @Operation(summary = "按溯源码查询使用记录")
    @GetMapping("/by-trace")
    public R<List<AgriUsage>> listByTrace(@RequestParam String traceCode) {
        return R.ok(agriUsageService.listByTraceCode(traceCode));
    }
}
