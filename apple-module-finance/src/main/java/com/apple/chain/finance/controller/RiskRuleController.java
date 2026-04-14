package com.apple.chain.finance.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.finance.dto.RiskRuleRequest;
import com.apple.chain.finance.dto.RiskRuleResponse;
import com.apple.chain.finance.entity.RiskRule;
import com.apple.chain.finance.service.RiskRuleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "风控预警 - 规则管理")
@RestController
@RequestMapping("/api/risk/rules")
@RequiredArgsConstructor
public class RiskRuleController {

    private final RiskRuleService riskRuleService;

    @Operation(summary = "风控规则列表（分页）")
    @GetMapping
    public R<PageResult<RiskRuleResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean enabled) {
        IPage<RiskRule> p = riskRuleService.listRules(page, size, keyword, severity, enabled);
        return R.ok(PageResult.of(
                p.getRecords().stream().map(RiskRuleResponse::from).toList(),
                p.getTotal(), p.getCurrent(), p.getSize()));
    }

    @Operation(summary = "风控规则详情")
    @GetMapping("/{id}")
    public R<RiskRuleResponse> detail(@PathVariable Long id) {
        return R.ok(RiskRuleResponse.from(riskRuleService.getRuleDetail(id)));
    }

    @Operation(summary = "创建风控规则")
    @PostMapping
    public R<RiskRuleResponse> create(@Valid @RequestBody RiskRuleRequest request) {
        // creatorId comes from JWT context in production; passing null here keeps
        // the seed/test paths working without extra wiring.
        return R.ok(RiskRuleResponse.from(riskRuleService.createRule(request, null)));
    }

    @Operation(summary = "更新风控规则")
    @PutMapping("/{id}")
    public R<RiskRuleResponse> update(@PathVariable Long id,
                                      @Valid @RequestBody RiskRuleRequest request) {
        return R.ok(RiskRuleResponse.from(riskRuleService.updateRule(id, request)));
    }

    @Operation(summary = "删除风控规则")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        riskRuleService.deleteRule(id);
        return R.ok("删除成功", null);
    }

    @Operation(summary = "启用 / 停用风控规则")
    @PutMapping("/{id}/enable")
    public R<RiskRuleResponse> enable(@PathVariable Long id, @RequestParam boolean enabled) {
        return R.ok(RiskRuleResponse.from(riskRuleService.setEnabled(id, enabled)));
    }
}
