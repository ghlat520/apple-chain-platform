package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdDqCheckResult;
import com.apple.chain.bigdata.entity.BdDqRule;
import com.apple.chain.bigdata.service.BdDqService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Data quality rule management + on-demand check.
 */
@Tag(name = "大数据-数据质量")
@RestController
@RequestMapping("/api/bigdata/dq")
@RequiredArgsConstructor
public class DqRuleController {

    private final BdDqService service;

    @Operation(summary = "分页查询规则")
    @GetMapping("/rules")
    public R<PageResult<BdDqRule>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Long assetId) {
        LambdaQueryWrapper<BdDqRule> wrapper = new LambdaQueryWrapper<>();
        if (ruleType != null) {
            wrapper.eq(BdDqRule::getRuleType, ruleType);
        }
        if (assetId != null) {
            wrapper.eq(BdDqRule::getAssetId, assetId);
        }
        wrapper.orderByDesc(BdDqRule::getId);
        return R.ok(PageResult.of(service.page(new Page<>(page, size), wrapper)));
    }

    @Operation(summary = "获取规则")
    @GetMapping("/rules/{id}")
    public R<BdDqRule> get(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    @Operation(summary = "新增规则")
    @PostMapping("/rules")
    public R<BdDqRule> create(@RequestBody BdDqRule body) {
        service.save(body);
        return R.ok(body);
    }

    @Operation(summary = "更新规则")
    @PutMapping("/rules/{id}")
    public R<BdDqRule> update(@PathVariable Long id, @RequestBody BdDqRule body) {
        body.setId(id);
        service.updateById(body);
        return R.ok(body);
    }

    @Operation(summary = "删除规则")
    @DeleteMapping("/rules/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "手动触发检查")
    @PostMapping("/rules/{id}/check")
    public R<BdDqCheckResult> check(@PathVariable Long id) {
        return R.ok(service.runCheck(id));
    }

    @Operation(summary = "检查结果历史")
    @GetMapping("/rules/{id}/results")
    public R<PageResult<BdDqCheckResult>> results(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(PageResult.of(service.listResults(id, page, size)));
    }
}
