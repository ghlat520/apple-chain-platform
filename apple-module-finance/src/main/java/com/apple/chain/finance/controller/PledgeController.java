package com.apple.chain.finance.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.finance.entity.Pledge;
import com.apple.chain.finance.service.PledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仓单质押")
@RestController
@RequestMapping("/api/finance/pledges")
@RequiredArgsConstructor
public class PledgeController {

    private final PledgeService pledgeService;

    @Operation(summary = "质押列表（分页）")
    @GetMapping("/list")
    public R<PageResult<Pledge>> list(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(pledgeService.listPledges(page, size, keyword, status)));
    }

    @Operation(summary = "质押详情")
    @GetMapping("/{id}")
    public R<Pledge> detail(@PathVariable Long id) { return R.ok(pledgeService.getPledgeDetail(id)); }

    @Operation(summary = "创建质押")
    @PostMapping
    public R<Pledge> create(@RequestBody Pledge pledge) { return R.ok(pledgeService.createPledge(pledge)); }

    @Operation(summary = "更新质押")
    @PutMapping("/{id}")
    public R<Pledge> update(@PathVariable Long id, @RequestBody Pledge pledge) { return R.ok(pledgeService.updatePledge(id, pledge)); }

    @Operation(summary = "激活质押")
    @PostMapping("/{id}/activate")
    public R<Pledge> activate(@PathVariable Long id) { return R.ok(pledgeService.activate(id)); }

    @Operation(summary = "解除质押")
    @PostMapping("/{id}/release")
    public R<Pledge> release(@PathVariable Long id) { return R.ok(pledgeService.release(id)); }

    @Operation(summary = "删除质押")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) { pledgeService.deletePledge(id); return R.ok("删除成功", null); }

    @Operation(summary = "导出质押CSV")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String keyword, @RequestParam(required = false) String status,
                       HttpServletResponse response) {
        pledgeService.exportPledges(keyword, status, response);
    }
}
