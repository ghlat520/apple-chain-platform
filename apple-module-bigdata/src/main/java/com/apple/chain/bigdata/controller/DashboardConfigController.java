package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdDashboardConfig;
import com.apple.chain.bigdata.entity.BdDashboardWidget;
import com.apple.chain.bigdata.service.BdDashboardConfigService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Big-screen (dashboard) layout configuration — M2 skeleton.
 * M4 will wire the drag-and-drop editor front-end.
 */
@Tag(name = "大数据-大屏配置")
@RestController
@RequestMapping("/api/bigdata/screen")
@RequiredArgsConstructor
public class DashboardConfigController {

    private final BdDashboardConfigService service;

    @Operation(summary = "分页查询大屏")
    @GetMapping
    public R<PageResult<BdDashboardConfig>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        LambdaQueryWrapper<BdDashboardConfig> wrapper = new LambdaQueryWrapper<>();
        if (category != null) {
            wrapper.eq(BdDashboardConfig::getCategory, category);
        }
        wrapper.orderByDesc(BdDashboardConfig::getId);
        return R.ok(PageResult.of(service.page(new Page<>(page, size), wrapper)));
    }

    @Operation(summary = "获取大屏")
    @GetMapping("/{id}")
    public R<BdDashboardConfig> get(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    @Operation(summary = "新增大屏")
    @PostMapping
    public R<BdDashboardConfig> create(@RequestBody BdDashboardConfig body) {
        service.save(body);
        return R.ok(body);
    }

    @Operation(summary = "更新大屏")
    @PutMapping("/{id}")
    public R<BdDashboardConfig> update(@PathVariable Long id, @RequestBody BdDashboardConfig body) {
        body.setId(id);
        service.updateById(body);
        return R.ok(body);
    }

    @Operation(summary = "删除大屏")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "发布大屏")
    @PostMapping("/{id}/publish")
    public R<Boolean> publish(@PathVariable Long id) {
        return R.ok(service.publish(id));
    }

    @Operation(summary = "列出 widget")
    @GetMapping("/{id}/widgets")
    public R<List<BdDashboardWidget>> widgets(@PathVariable Long id) {
        return R.ok(service.listWidgets(id));
    }

    @Operation(summary = "新增 widget")
    @PostMapping("/{id}/widgets")
    public R<BdDashboardWidget> addWidget(@PathVariable Long id,
                                          @RequestBody BdDashboardWidget widget) {
        widget.setDashboardId(id);
        return R.ok(service.addWidget(widget));
    }

    @Operation(summary = "删除 widget")
    @DeleteMapping("/widgets/{widgetId}")
    public R<Boolean> deleteWidget(@PathVariable Long widgetId) {
        return R.ok(service.removeWidget(widgetId));
    }
}
