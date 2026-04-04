package com.apple.chain.planting.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.planting.entity.Farmer;
import com.apple.chain.planting.service.FarmerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Farmer management endpoints.
 * Path: /api/farm/farmers
 */
@Tag(name = "农户管理")
@RestController
@RequestMapping("/api/farm/farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerService farmerService;

    @Operation(summary = "农户列表（分页）")
    @GetMapping
    public R<PageResult<Farmer>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(farmerService.listFarmers(page, size, keyword, status)));
    }

    @Operation(summary = "农户详情")
    @GetMapping("/{id}")
    public R<Farmer> detail(@PathVariable Long id) {
        return R.ok(farmerService.getFarmerDetail(id));
    }

    @Operation(summary = "创建农户")
    @PostMapping
    public R<Farmer> create(@Valid @RequestBody Farmer farmer) {
        return R.ok(farmerService.createFarmer(farmer));
    }

    @Operation(summary = "更新农户")
    @PutMapping("/{id}")
    public R<Farmer> update(@PathVariable Long id, @RequestBody Farmer farmer) {
        return R.ok(farmerService.updateFarmer(id, farmer));
    }

    @Operation(summary = "删除农户（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出农户CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        farmerService.exportFarmers(keyword, status, response);
    }
}
