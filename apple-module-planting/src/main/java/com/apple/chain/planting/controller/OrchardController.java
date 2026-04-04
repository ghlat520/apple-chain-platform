package com.apple.chain.planting.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.service.OrchardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Orchard management endpoints.
 */
@Tag(name = "果园管理")
@RestController
@RequestMapping("/api/planting/orchard")
@RequiredArgsConstructor
public class OrchardController {

    private final OrchardService orchardService;

    @Operation(summary = "果园列表（分页）")
    @GetMapping("/list")
    public R<PageResult<Orchard>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId) {
        return R.ok(PageResult.of(orchardService.listOrchards(page, size, keyword, status, farmerId)));
    }

    @Operation(summary = "果园详情")
    @GetMapping("/{id}")
    public R<Orchard> detail(@PathVariable Long id) {
        return R.ok(orchardService.getOrchardDetail(id));
    }

    @Operation(summary = "创建果园")
    @PostMapping
    public R<Orchard> create(@RequestBody Orchard orchard) {
        return R.ok(orchardService.createOrchard(orchard));
    }

    @Operation(summary = "更新果园")
    @PutMapping("/{id}")
    public R<Orchard> update(@PathVariable Long id, @RequestBody Orchard orchard) {
        return R.ok(orchardService.updateOrchard(id, orchard));
    }

    @Operation(summary = "删除果园（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        orchardService.deleteOrchard(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出果园CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId,
            HttpServletResponse response) {
        orchardService.exportOrchards(keyword, status, farmerId, response);
    }
}
