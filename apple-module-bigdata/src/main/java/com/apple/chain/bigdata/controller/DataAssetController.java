package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdDataAsset;
import com.apple.chain.bigdata.entity.BdDataAssetField;
import com.apple.chain.bigdata.service.BdDataAssetService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Data asset catalog — table + field metadata browser.
 */
@Tag(name = "大数据-数据资产目录")
@RestController
@RequestMapping("/api/bigdata/asset")
@RequiredArgsConstructor
public class DataAssetController {

    private final BdDataAssetService service;

    @Operation(summary = "分页查询资产")
    @GetMapping
    public R<PageResult<BdDataAsset>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String securityLevel) {
        LambdaQueryWrapper<BdDataAsset> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BdDataAsset::getAssetCode, keyword)
                    .or().like(BdDataAsset::getBizName, keyword));
        }
        if (StringUtils.hasText(securityLevel)) {
            wrapper.eq(BdDataAsset::getSecurityLevel, securityLevel);
        }
        wrapper.orderByDesc(BdDataAsset::getId);
        return R.ok(PageResult.of(service.page(new Page<>(page, size), wrapper)));
    }

    @Operation(summary = "获取资产详情")
    @GetMapping("/{id}")
    public R<BdDataAsset> get(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    @Operation(summary = "新增资产")
    @PostMapping
    public R<BdDataAsset> create(@RequestBody BdDataAsset body) {
        service.save(body);
        return R.ok(body);
    }

    @Operation(summary = "更新资产")
    @PutMapping("/{id}")
    public R<BdDataAsset> update(@PathVariable Long id, @RequestBody BdDataAsset body) {
        body.setId(id);
        service.updateById(body);
        return R.ok(body);
    }

    @Operation(summary = "删除资产")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @Operation(summary = "列出资产字段")
    @GetMapping("/{id}/fields")
    public R<List<BdDataAssetField>> listFields(@PathVariable Long id) {
        return R.ok(service.listFields(id));
    }

    @Operation(summary = "新增字段")
    @PostMapping("/{id}/fields")
    public R<BdDataAssetField> addField(@PathVariable Long id,
                                        @RequestBody BdDataAssetField field) {
        field.setAssetId(id);
        return R.ok(service.addField(field));
    }

    @Operation(summary = "删除字段")
    @DeleteMapping("/fields/{fieldId}")
    public R<Boolean> deleteField(@PathVariable Long fieldId) {
        return R.ok(service.removeField(fieldId));
    }
}
