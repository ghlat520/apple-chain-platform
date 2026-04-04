package com.apple.chain.trade.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trade.entity.SupplyInfo;
import com.apple.chain.trade.service.SupplyInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Supply info endpoints.
 */
@Tag(name = "供货管理")
@RestController
@RequestMapping("/api/trade/supply")
@RequiredArgsConstructor
public class SupplyInfoController {

    private final SupplyInfoService supplyInfoService;

    @Operation(summary = "供货列表（分页）")
    @GetMapping("/list")
    public R<PageResult<SupplyInfo>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String variety,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId) {
        return R.ok(PageResult.of(supplyInfoService.listSupplies(page, size, keyword, variety, status, farmerId)));
    }

    @Operation(summary = "供货详情")
    @GetMapping("/{id}")
    public R<SupplyInfo> detail(@PathVariable Long id) {
        return R.ok(supplyInfoService.getSupplyDetail(id));
    }

    @Operation(summary = "创建供货信息")
    @PostMapping
    public R<SupplyInfo> create(@RequestBody SupplyInfo supply) {
        return R.ok(supplyInfoService.createSupply(supply));
    }

    @Operation(summary = "更新供货信息")
    @PutMapping("/{id}")
    public R<SupplyInfo> update(@PathVariable Long id, @RequestBody SupplyInfo supply) {
        return R.ok(supplyInfoService.updateSupply(id, supply));
    }

    @Operation(summary = "发布供货信息")
    @PutMapping("/{id}/publish")
    public R<SupplyInfo> publish(@PathVariable Long id) {
        return R.ok(supplyInfoService.publishSupply(id));
    }

    @Operation(summary = "删除供货信息")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        supplyInfoService.deleteSupply(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出供货信息CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String variety,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long farmerId,
            HttpServletResponse response) {
        supplyInfoService.exportSupplies(keyword, variety, status, farmerId, response);
    }
}
