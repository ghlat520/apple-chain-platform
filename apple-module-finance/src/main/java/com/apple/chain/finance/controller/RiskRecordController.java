package com.apple.chain.finance.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.finance.entity.RiskRecord;
import com.apple.chain.finance.service.RiskRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "风控管理")
@RestController
@RequestMapping("/api/finance/risks")
@RequiredArgsConstructor
public class RiskRecordController {

    private final RiskRecordService riskRecordService;

    @Operation(summary = "风控记录列表（分页）")
    @GetMapping("/list")
    public R<PageResult<RiskRecord>> list(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(riskRecordService.listRecords(page, size, keyword, riskLevel, status)));
    }

    @Operation(summary = "风控记录详情")
    @GetMapping("/{id}")
    public R<RiskRecord> detail(@PathVariable Long id) { return R.ok(riskRecordService.getRecordDetail(id)); }

    @Operation(summary = "创建风控记录")
    @PostMapping
    public R<RiskRecord> create(@RequestBody RiskRecord record) { return R.ok(riskRecordService.createRecord(record)); }

    @Operation(summary = "更新风控记录")
    @PutMapping("/{id}")
    public R<RiskRecord> update(@PathVariable Long id, @RequestBody RiskRecord record) { return R.ok(riskRecordService.updateRecord(id, record)); }

    @Operation(summary = "删除风控记录")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) { riskRecordService.deleteRecord(id); return R.ok("删除成功", null); }

    @Operation(summary = "导出风控记录CSV")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String keyword, @RequestParam(required = false) String riskLevel,
                       @RequestParam(required = false) String status, HttpServletResponse response) {
        riskRecordService.exportRecords(keyword, riskLevel, status, response);
    }
}
