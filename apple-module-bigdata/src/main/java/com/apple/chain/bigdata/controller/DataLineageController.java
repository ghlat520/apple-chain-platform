package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdDataLineage;
import com.apple.chain.bigdata.service.BdDataLineageService;
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
 * Data lineage (DataOps concept) — upstream/downstream DAG.
 * Distinct from product trace chain (apple-module-trace / blockchain).
 */
@Tag(name = "大数据-数据血缘")
@RestController
@RequestMapping("/api/bigdata/lineage")
@RequiredArgsConstructor
public class DataLineageController {

    private final BdDataLineageService service;

    @Operation(summary = "分页查询血缘边")
    @GetMapping
    public R<PageResult<BdDataLineage>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String relationType) {
        LambdaQueryWrapper<BdDataLineage> wrapper = new LambdaQueryWrapper<>();
        if (relationType != null) {
            wrapper.eq(BdDataLineage::getRelationType, relationType);
        }
        wrapper.orderByDesc(BdDataLineage::getId);
        return R.ok(PageResult.of(service.page(new Page<>(page, size), wrapper)));
    }

    @Operation(summary = "新增血缘边")
    @PostMapping
    public R<BdDataLineage> create(@RequestBody BdDataLineage body) {
        service.save(body);
        return R.ok(body);
    }

    @Operation(summary = "删除血缘边")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "下游依赖")
    @GetMapping("/downstream")
    public R<List<BdDataLineage>> downstream(@RequestParam String type,
                                             @RequestParam String id) {
        return R.ok(service.findDownstream(type, id));
    }

    @Operation(summary = "上游血缘")
    @GetMapping("/upstream")
    public R<List<BdDataLineage>> upstream(@RequestParam String type,
                                           @RequestParam String id) {
        return R.ok(service.findUpstream(type, id));
    }
}
