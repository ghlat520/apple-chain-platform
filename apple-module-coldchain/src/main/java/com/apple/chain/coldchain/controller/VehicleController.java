package com.apple.chain.coldchain.controller;

import com.apple.chain.coldchain.entity.Vehicle;
import com.apple.chain.coldchain.service.VehicleService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "车辆管理")
@RestController
@RequestMapping("/api/coldchain/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "车辆列表（分页）")
    @GetMapping("/list")
    public R<PageResult<Vehicle>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(vehicleService.listVehicles(page, size, keyword, vehicleType, status)));
    }

    @Operation(summary = "车辆详情")
    @GetMapping("/{id}")
    public R<Vehicle> detail(@PathVariable Long id) {
        return R.ok(vehicleService.getVehicleDetail(id));
    }

    @Operation(summary = "创建车辆")
    @PostMapping
    public R<Vehicle> create(@RequestBody Vehicle vehicle) {
        return R.ok(vehicleService.createVehicle(vehicle));
    }

    @Operation(summary = "更新车辆")
    @PutMapping("/{id}")
    public R<Vehicle> update(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return R.ok(vehicleService.updateVehicle(id, vehicle));
    }

    @Operation(summary = "删除车辆")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "变更车辆状态")
    @PutMapping("/{id}/status")
    public R<Vehicle> changeStatus(@PathVariable Long id, @RequestParam String newStatus) {
        return R.ok(vehicleService.changeStatus(id, newStatus));
    }

    @Operation(summary = "导出车辆CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        vehicleService.exportVehicles(keyword, vehicleType, status, response);
    }
}
