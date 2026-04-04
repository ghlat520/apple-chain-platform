package com.apple.chain.input.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.input.entity.AgriProduct;
import com.apple.chain.input.service.AgriProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Agricultural product endpoints.
 */
@Tag(name = "农资产品")
@RestController
@RequestMapping("/api/input/products")
@RequiredArgsConstructor
public class AgriProductController {

    private final AgriProductService agriProductService;

    @Operation(summary = "农资产品列表（分页）")
    @GetMapping("/list")
    public R<PageResult<AgriProduct>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(agriProductService.listProducts(page, size, keyword, type, status)));
    }

    @Operation(summary = "农资产品详情")
    @GetMapping("/{id}")
    public R<AgriProduct> detail(@PathVariable Long id) {
        return R.ok(agriProductService.getDetail(id));
    }

    @Operation(summary = "创建农资产品")
    @PostMapping
    public R<AgriProduct> create(@RequestBody AgriProduct product) {
        return R.ok(agriProductService.createProduct(product));
    }

    @Operation(summary = "更新农资产品")
    @PutMapping("/{id}")
    public R<AgriProduct> update(@PathVariable Long id, @RequestBody AgriProduct product) {
        return R.ok(agriProductService.updateProduct(id, product));
    }

    @Operation(summary = "删除农资产品")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        agriProductService.deleteProduct(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出农资产品CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        agriProductService.exportProducts(keyword, type, status, response);
    }
}
