package com.apple.chain.planting.controller;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.GrowthRecord;
import com.apple.chain.planting.mapper.GrowthRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * Growth record CRUD endpoints.
 * Uses inline ServiceImpl to keep file count manageable.
 */
@Tag(name = "农事记录管理")
@RestController
@RequestMapping("/api/planting/record")
@RequiredArgsConstructor
public class GrowthRecordController {

    private final GrowthRecordMapper growthRecordMapper;

    @Operation(summary = "农事记录列表（分页）")
    @GetMapping("/list")
    public R<PageResult<GrowthRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long orchardId,
            @RequestParam(required = false) String recordType) {
        LambdaQueryWrapper<GrowthRecord> wrapper = new LambdaQueryWrapper<GrowthRecord>()
                .eq(orchardId != null, GrowthRecord::getOrchardId, orchardId)
                .eq(StringUtils.hasText(recordType), GrowthRecord::getRecordType, recordType)
                .orderByDesc(GrowthRecord::getOperateDate);
        IPage<GrowthRecord> result = growthRecordMapper.selectPage(new Page<>(page, size), wrapper);
        return R.ok(PageResult.of(result));
    }

    @Operation(summary = "农事记录详情")
    @GetMapping("/{id}")
    public R<GrowthRecord> detail(@PathVariable Long id) {
        GrowthRecord record = growthRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND, "记录不存在");
        }
        return R.ok(record);
    }

    @Operation(summary = "创建农事记录")
    @PostMapping
    public R<GrowthRecord> create(@RequestBody GrowthRecord record) {
        growthRecordMapper.insert(record);
        return R.ok(record);
    }

    @Operation(summary = "更新农事记录")
    @PutMapping("/{id}")
    public R<GrowthRecord> update(@PathVariable Long id, @RequestBody GrowthRecord record) {
        if (growthRecordMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "记录不存在");
        }
        record.setId(id);
        growthRecordMapper.updateById(record);
        return R.ok(growthRecordMapper.selectById(id));
    }

    @Operation(summary = "删除农事记录（软删除）")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (growthRecordMapper.deleteById(id) == 0) {
            throw new BizException(ResultCode.NOT_FOUND, "记录不存在");
        }
        return R.ok("删除成功", null);
    }
}
