package com.apple.chain.input.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.input.entity.AgriSupplier;
import com.apple.chain.input.service.AgriSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Agricultural input supplier endpoints.
 */
@Tag(name = "农资供应商")
@RestController
@RequestMapping("/api/input/suppliers")
@RequiredArgsConstructor
public class AgriSupplierController {

    private final AgriSupplierService agriSupplierService;

    @Operation(summary = "供应商列表（分页）")
    @GetMapping("/list")
    public R<PageResult<AgriSupplier>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(agriSupplierService.listSuppliers(page, size, keyword, status)));
    }

    @Operation(summary = "供应商详情")
    @GetMapping("/{id}")
    public R<AgriSupplier> detail(@PathVariable Long id) {
        return R.ok(agriSupplierService.getDetail(id));
    }

    @Operation(summary = "创建供应商")
    @PostMapping
    public R<AgriSupplier> create(@RequestBody AgriSupplier supplier) {
        return R.ok(agriSupplierService.createSupplier(supplier));
    }

    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    public R<AgriSupplier> update(@PathVariable Long id, @RequestBody AgriSupplier supplier) {
        return R.ok(agriSupplierService.updateSupplier(id, supplier));
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        agriSupplierService.deleteSupplier(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "审核供应商 (decision: APPROVE/REJECT/BLACKLIST)")
    @PutMapping("/{id}/audit")
    public R<AgriSupplier> audit(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String decision = body.get("decision");
        String reason = body.get("reason");
        return R.ok(agriSupplierService.auditSupplier(id, decision, reason));
    }

    @Operation(summary = "重新提交审核（REJECTED→PENDING）")
    @PutMapping("/{id}/reinstate")
    public R<AgriSupplier> reinstate(@PathVariable Long id) {
        return R.ok(agriSupplierService.reinstateSupplier(id));
    }

    @Operation(summary = "导出供应商CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        agriSupplierService.exportSuppliers(keyword, status, response);
    }
}
