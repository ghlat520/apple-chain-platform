package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdMetricDefinition;
import com.apple.chain.bigdata.entity.BdMetricValue;
import com.apple.chain.bigdata.service.BdMetricService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Metric center — unified indicator definitions + value snapshots.
 */
@Tag(name = "大数据-指标中心")
@RestController
@RequestMapping("/api/bigdata/metric")
@RequiredArgsConstructor
public class MetricController {

    private final BdMetricService service;

    @Operation(summary = "分页查询指标")
    @GetMapping
    public R<PageResult<BdMetricDefinition>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer isCore) {
        LambdaQueryWrapper<BdMetricDefinition> wrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            wrapper.eq(BdMetricDefinition::getCategory, category);
        }
        if (isCore != null) {
            wrapper.eq(BdMetricDefinition::getIsCore, isCore);
        }
        wrapper.orderByDesc(BdMetricDefinition::getId);
        return R.ok(PageResult.of(service.page(new Page<>(page, size), wrapper)));
    }

    @Operation(summary = "指标详情")
    @GetMapping("/{id}")
    public R<BdMetricDefinition> get(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    @Operation(summary = "新增指标")
    @PostMapping
    public R<BdMetricDefinition> create(@RequestBody BdMetricDefinition body) {
        service.save(body);
        return R.ok(body);
    }

    @Operation(summary = "更新指标")
    @PutMapping("/{id}")
    public R<BdMetricDefinition> update(@PathVariable Long id, @RequestBody BdMetricDefinition body) {
        body.setId(id);
        service.updateById(body);
        return R.ok(body);
    }

    @Operation(summary = "删除指标")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "指标取值序列")
    @GetMapping("/{code}/values")
    public R<List<BdMetricValue>> values(
            @PathVariable String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return R.ok(service.listValues(code, from, to));
    }

    @Operation(summary = "记录指标取值")
    @PostMapping("/values")
    public R<BdMetricValue> recordValue(@RequestBody BdMetricValue body) {
        return R.ok(service.recordValue(body));
    }
}
