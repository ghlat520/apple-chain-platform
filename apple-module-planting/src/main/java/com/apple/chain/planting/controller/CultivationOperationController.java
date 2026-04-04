package com.apple.chain.planting.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.CultivationOperation;
import com.apple.chain.planting.service.CultivationOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Cultivation operation endpoints.
 * Path: /api/cultivation/operations
 *
 * Records individual farming operations (fertilize/pesticide/prune/harvest/irrigate).
 */
@Tag(name = "农事操作管理")
@RestController
@RequestMapping("/api/cultivation/operations")
@RequiredArgsConstructor
public class CultivationOperationController {

    private final CultivationOperationService operationService;

    @Operation(summary = "操作记录列表（分页 + batchId + operationType）")
    @GetMapping
    public R<PageResult<CultivationOperation>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String operationType) {
        return R.ok(PageResult.of(operationService.listOperations(page, size, batchId, operationType)));
    }

    @Operation(summary = "创建操作记录")
    @PostMapping
    public R<CultivationOperation> create(@Valid @RequestBody CultivationOperation operation) {
        return R.ok(operationService.createOperation(operation));
    }

    @Operation(summary = "更新操作记录")
    @PutMapping("/{id}")
    public R<CultivationOperation> update(@PathVariable Long id, @RequestBody CultivationOperation operation) {
        return R.ok(operationService.updateOperation(id, operation));
    }

    @Operation(summary = "删除操作记录（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        operationService.deleteOperation(id);
        return R.ok("删除成功", null);
    }
}
