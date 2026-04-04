package com.apple.chain.coldchain.controller;

import com.apple.chain.coldchain.entity.Delivery;
import com.apple.chain.coldchain.service.DeliveryService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "配送管理")
@RestController
@RequestMapping("/api/coldchain/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "配送列表（分页）")
    @GetMapping("/list")
    public R<PageResult<Delivery>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(deliveryService.listDeliveries(page, size, keyword, status)));
    }

    @Operation(summary = "配送详情")
    @GetMapping("/{id}")
    public R<Delivery> detail(@PathVariable Long id) {
        return R.ok(deliveryService.getDeliveryDetail(id));
    }

    @Operation(summary = "创建配送")
    @PostMapping
    public R<Delivery> create(@RequestBody Delivery delivery) {
        return R.ok(deliveryService.createDelivery(delivery));
    }

    @Operation(summary = "更新配送")
    @PutMapping("/{id}")
    public R<Delivery> update(@PathVariable Long id, @RequestBody Delivery delivery) {
        return R.ok(deliveryService.updateDelivery(id, delivery));
    }

    @Operation(summary = "签收确认")
    @PostMapping("/{id}/sign")
    public R<Delivery> sign(@PathVariable Long id, @RequestBody Delivery delivery) {
        return R.ok(deliveryService.sign(id, delivery));
    }

    @Operation(summary = "删除配送")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出配送CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        deliveryService.exportDeliveries(keyword, status, response);
    }
}
