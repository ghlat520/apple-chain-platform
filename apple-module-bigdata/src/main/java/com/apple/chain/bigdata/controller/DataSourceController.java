package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdDataSource;
import com.apple.chain.bigdata.service.BdDataSourceService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Data source registry — 8 internal modules + 4 external APIs.
 */
@Tag(name = "大数据-数据源注册")
@RestController
@RequestMapping("/api/bigdata/source")
@RequiredArgsConstructor
public class DataSourceController {

    private final BdDataSourceService service;

    @Operation(summary = "分页查询数据源")
    @GetMapping
    public R<PageResult<BdDataSource>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BdDataSource> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(BdDataSource::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BdDataSource::getSourceName, keyword)
                    .or().like(BdDataSource::getSourceCode, keyword));
        }
        wrapper.orderByDesc(BdDataSource::getId);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BdDataSource> result =
                service.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
        result.getRecords().forEach(ds -> ds.setPasswordEnc(null));
        return R.ok(PageResult.of(result));
    }

    @Operation(summary = "获取数据源详情")
    @GetMapping("/{id}")
    public R<BdDataSource> get(@PathVariable Long id) {
        BdDataSource ds = service.getById(id);
        if (ds != null) ds.setPasswordEnc(null);
        return R.ok(ds);
    }

    @Operation(summary = "新增数据源")
    @PostMapping
    public R<BdDataSource> create(@RequestBody BdDataSource body) {
        service.save(body);
        body.setPasswordEnc(null);
        return R.ok(body);
    }

    @Operation(summary = "更新数据源")
    @PutMapping("/{id}")
    public R<BdDataSource> update(@PathVariable Long id, @RequestBody BdDataSource body) {
        body.setId(id);
        service.updateById(body);
        body.setPasswordEnc(null);
        return R.ok(body);
    }

    @Operation(summary = "删除数据源")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "连通性测试")
    @PostMapping("/{id}/test")
    public R<String> test(@PathVariable Long id) {
        return R.ok(service.testConnection(id));
    }
}
