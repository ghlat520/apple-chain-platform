package com.apple.chain.finance.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.finance.dto.RiskEventHandleRequest;
import com.apple.chain.finance.dto.RiskEventResponse;
import com.apple.chain.finance.entity.RiskEvent;
import com.apple.chain.finance.service.RiskEventService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "风控预警 - 事件管理")
@RestController
@RequestMapping("/api/risk/events")
@RequiredArgsConstructor
public class RiskEventController {

    private final RiskEventService riskEventService;

    @Operation(summary = "风控事件列表（分页 + 过滤）")
    @GetMapping
    public R<PageResult<RiskEventResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType) {
        IPage<RiskEvent> p = riskEventService.listEvents(page, size, severity, status, targetType);
        return R.ok(PageResult.of(
                p.getRecords().stream().map(RiskEventResponse::from).toList(),
                p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @Operation(summary = "风控事件详情")
    @GetMapping("/{id}")
    public R<RiskEventResponse> detail(@PathVariable Long id) {
        return R.ok(RiskEventResponse.from(riskEventService.getEventDetail(id)));
    }

    @Operation(summary = "处置风控事件 (PENDING/HANDLING → HANDLING/RESOLVED/IGNORED)")
    @PutMapping("/{id}/handle")
    public R<RiskEventResponse> handle(@PathVariable Long id,
                                       @Valid @RequestBody RiskEventHandleRequest request) {
        return R.ok(RiskEventResponse.from(riskEventService.handleEvent(id, request)));
    }
}
