package com.apple.chain.finance.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.finance.entity.CreditRating;
import com.apple.chain.finance.service.CreditRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "信用评级")
@RestController
@RequestMapping("/api/finance/credits")
@RequiredArgsConstructor
public class CreditRatingController {

    private final CreditRatingService creditRatingService;

    @Operation(summary = "信用评级列表（分页）")
    @GetMapping("/list")
    public R<PageResult<CreditRating>> list(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String creditLevel) {
        return R.ok(PageResult.of(creditRatingService.listRatings(page, size, keyword, entityType, creditLevel)));
    }

    @Operation(summary = "信用评级详情")
    @GetMapping("/{id}")
    public R<CreditRating> detail(@PathVariable Long id) { return R.ok(creditRatingService.getRatingDetail(id)); }

    @Operation(summary = "创建信用评级")
    @PostMapping
    public R<CreditRating> create(@RequestBody CreditRating rating) { return R.ok(creditRatingService.createRating(rating)); }

    @Operation(summary = "更新信用评级")
    @PutMapping("/{id}")
    public R<CreditRating> update(@PathVariable Long id, @RequestBody CreditRating rating) { return R.ok(creditRatingService.updateRating(id, rating)); }

    @Operation(summary = "删除信用评级")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) { creditRatingService.deleteRating(id); return R.ok("删除成功", null); }

    @Operation(summary = "计算信用评分")
    @PostMapping("/{entityId}/calculate")
    public R<CreditRating> calculate(@PathVariable Long entityId,
                                     @RequestParam String entityType) {
        return R.ok(creditRatingService.calculateScore(entityId, entityType));
    }

    @Operation(summary = "导出信用评级CSV")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String keyword, @RequestParam(required = false) String entityType,
                       @RequestParam(required = false) String creditLevel, HttpServletResponse response) {
        creditRatingService.exportRatings(keyword, entityType, creditLevel, response);
    }
}
