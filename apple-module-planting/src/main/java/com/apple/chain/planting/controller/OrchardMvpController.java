package com.apple.chain.planting.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.OrchardMvp;
import com.apple.chain.planting.service.OrchardMvpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * MVP Orchard management endpoints.
 * Path: /api/farm/orchards
 */
@Tag(name = "果园管理(MVP)")
@RestController
@RequestMapping("/api/farm/orchards")
@RequiredArgsConstructor
public class OrchardMvpController {

    private final OrchardMvpService orchardMvpService;

    @Operation(summary = "果园列表（分页 + keyword + status）")
    @GetMapping
    public R<PageResult<OrchardMvp>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(orchardMvpService.listOrchards(page, size, keyword, status)));
    }

    @Operation(summary = "果园详情")
    @GetMapping("/{id}")
    public R<OrchardMvp> detail(@PathVariable Long id) {
        return R.ok(orchardMvpService.getOrchardDetail(id));
    }

    @Operation(summary = "创建果园（自动生成orchardCode）")
    @PostMapping
    public R<OrchardMvp> create(@Valid @RequestBody OrchardMvp orchard) {
        return R.ok(orchardMvpService.createOrchard(orchard));
    }

    @Operation(summary = "更新果园")
    @PutMapping("/{id}")
    public R<OrchardMvp> update(@PathVariable Long id, @RequestBody OrchardMvp orchard) {
        return R.ok(orchardMvpService.updateOrchard(id, orchard));
    }

    @Operation(summary = "删除果园（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        orchardMvpService.deleteOrchard(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出果园CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        orchardMvpService.exportOrchards(keyword, status, response);
    }
}
