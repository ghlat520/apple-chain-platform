package com.apple.chain.trade.controller;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.PurchaseNeed;
import com.apple.chain.trade.mapper.PurchaseNeedMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Purchase need endpoints.
 */
@Tag(name = "采购需求管理")
@RestController
@RequestMapping("/api/trade/need")
@RequiredArgsConstructor
public class PurchaseNeedController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PurchaseNeedMapper purchaseNeedMapper;

    @Operation(summary = "采购需求列表（分页）")
    @GetMapping("/list")
    public R<PageResult<PurchaseNeed>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String variety,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long buyerId) {
        LambdaQueryWrapper<PurchaseNeed> wrapper = new LambdaQueryWrapper<PurchaseNeed>()
                .like(StringUtils.hasText(keyword), PurchaseNeed::getNeedNo, keyword)
                .eq(StringUtils.hasText(variety), PurchaseNeed::getVariety, variety)
                .eq(StringUtils.hasText(status), PurchaseNeed::getStatus, status)
                .eq(buyerId != null, PurchaseNeed::getBuyerId, buyerId)
                .orderByDesc(PurchaseNeed::getCreateTime);
        IPage<PurchaseNeed> result = purchaseNeedMapper.selectPage(new Page<>(page, size), wrapper);
        return R.ok(PageResult.of(result));
    }

    @Operation(summary = "采购需求详情")
    @GetMapping("/{id}")
    public R<PurchaseNeed> detail(@PathVariable Long id) {
        PurchaseNeed need = purchaseNeedMapper.selectById(id);
        if (need == null) {
            throw new BizException(ResultCode.NOT_FOUND, "采购需求不存在");
        }
        return R.ok(need);
    }

    @Operation(summary = "创建采购需求")
    @PostMapping
    public R<PurchaseNeed> create(@RequestBody PurchaseNeed need) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = purchaseNeedMapper.nextSeq(prefix);
        need.setNeedNo(String.format("NED%s%04d", prefix, seq));
        need.setStatus("DRAFT");
        purchaseNeedMapper.insert(need);
        return R.ok(need);
    }

    @Operation(summary = "更新采购需求")
    @PutMapping("/{id}")
    public R<PurchaseNeed> update(@PathVariable Long id, @RequestBody PurchaseNeed need) {
        PurchaseNeed existing = purchaseNeedMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ResultCode.NOT_FOUND, "采购需求不存在");
        }
        need.setId(id);
        need.setNeedNo(null);
        purchaseNeedMapper.updateById(need);
        return R.ok(purchaseNeedMapper.selectById(id));
    }

    @Operation(summary = "发布采购需求")
    @PutMapping("/{id}/publish")
    public R<PurchaseNeed> publish(@PathVariable Long id) {
        PurchaseNeed need = purchaseNeedMapper.selectById(id);
        if (need == null) {
            throw new BizException(ResultCode.NOT_FOUND, "采购需求不存在");
        }
        if (!"DRAFT".equals(need.getStatus())) {
            throw new BizException("只有草稿状态可以发布");
        }
        PurchaseNeed update = new PurchaseNeed();
        update.setId(id);
        update.setStatus("PUBLISHED");
        purchaseNeedMapper.updateById(update);
        return R.ok(purchaseNeedMapper.selectById(id));
    }

    @Operation(summary = "删除采购需求")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        if (purchaseNeedMapper.deleteById(id) == 0) {
            throw new BizException(ResultCode.NOT_FOUND, "采购需求不存在");
        }
        return R.ok("删除成功", null);
    }

    @Operation(summary = "导出采购需求CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String variety,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long buyerId,
            HttpServletResponse response) {
        LambdaQueryWrapper<PurchaseNeed> wrapper = new LambdaQueryWrapper<PurchaseNeed>()
                .like(StringUtils.hasText(keyword), PurchaseNeed::getNeedNo, keyword)
                .eq(StringUtils.hasText(variety), PurchaseNeed::getVariety, variety)
                .eq(StringUtils.hasText(status), PurchaseNeed::getStatus, status)
                .eq(buyerId != null, PurchaseNeed::getBuyerId, buyerId)
                .orderByDesc(PurchaseNeed::getCreateTime);
        List<PurchaseNeed> list = purchaseNeedMapper.selectList(wrapper);
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("采购需求.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("需求编号,买家ID,品种,数量(kg),最高价格(元/kg),需求日期,质量等级,状态");
            for (PurchaseNeed n : list) {
                writer.println(
                        n.getNeedNo() + "," + n.getBuyerId() + "," + n.getVariety() + "," +
                        n.getQuantity() + "," + n.getPriceMax() + "," +
                        n.getRequireDate() + "," + n.getQuality() + "," + n.getStatus()
                );
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("导出失败: " + e.getMessage());
        }
    }
}
