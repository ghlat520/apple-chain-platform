package com.apple.chain.trade.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trade.entity.QualityInspection;
import com.apple.chain.trade.service.QualityInspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "质量检验")
@RestController
@RequestMapping("/api/trade/inspection")
@RequiredArgsConstructor
public class QualityInspectionController {

    private final QualityInspectionService inspectionService;

    @Operation(summary = "创建质检单")
    @PostMapping
    public R<QualityInspection> create(@RequestBody QualityInspection inspection) {
        return R.ok(inspectionService.createInspection(inspection));
    }

    @Operation(summary = "质检列表")
    @GetMapping
    public R<PageResult<QualityInspection>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String result) {
        return R.ok(PageResult.of(inspectionService.listInspections(page, size, orderId, status, result)));
    }

    @Operation(summary = "质检详情")
    @GetMapping("/{id}")
    public R<QualityInspection> detail(@PathVariable Long id) {
        return R.ok(inspectionService.getDetail(id));
    }

    @Operation(summary = "录入检验结果")
    @PutMapping("/{id}/inspect")
    public R<QualityInspection> inspect(@PathVariable Long id, @RequestBody QualityInspection data) {
        return R.ok(inspectionService.inspect(id, data));
    }

    @Operation(summary = "验收通过")
    @PutMapping("/{id}/accept")
    public R<QualityInspection> accept(@PathVariable Long id) {
        return R.ok(inspectionService.accept(id));
    }

    @Operation(summary = "发起争议")
    @PutMapping("/{id}/dispute")
    public R<QualityInspection> dispute(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(inspectionService.dispute(id, body.get("reason")));
    }
}
