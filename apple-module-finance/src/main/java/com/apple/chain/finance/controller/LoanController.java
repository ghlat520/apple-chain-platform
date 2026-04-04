package com.apple.chain.finance.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.finance.entity.Loan;
import com.apple.chain.finance.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "贷款管理")
@RestController
@RequestMapping("/api/finance/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "贷款列表（分页）")
    @GetMapping("/list")
    public R<PageResult<Loan>> list(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String loanType,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(loanService.listLoans(page, size, keyword, loanType, status)));
    }

    @Operation(summary = "贷款详情")
    @GetMapping("/{id}")
    public R<Loan> detail(@PathVariable Long id) { return R.ok(loanService.getLoanDetail(id)); }

    @Operation(summary = "创建贷款申请")
    @PostMapping
    public R<Loan> create(@RequestBody Loan loan) { return R.ok(loanService.createLoan(loan)); }

    @Operation(summary = "更新贷款")
    @PutMapping("/{id}")
    public R<Loan> update(@PathVariable Long id, @RequestBody Loan loan) { return R.ok(loanService.updateLoan(id, loan)); }

    @Operation(summary = "审批通过")
    @PostMapping("/{id}/approve")
    public R<Loan> approve(@PathVariable Long id) { return R.ok(loanService.approve(id)); }

    @Operation(summary = "审批拒绝")
    @PostMapping("/{id}/reject")
    public R<Loan> reject(@PathVariable Long id) { return R.ok(loanService.reject(id)); }

    @Operation(summary = "放款")
    @PostMapping("/{id}/disburse")
    public R<Loan> disburse(@PathVariable Long id) { return R.ok(loanService.disburse(id)); }

    @Operation(summary = "删除贷款")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) { loanService.deleteLoan(id); return R.ok("删除成功", null); }

    @Operation(summary = "导出贷款CSV")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String keyword, @RequestParam(required = false) String loanType,
                       @RequestParam(required = false) String status, HttpServletResponse response) {
        loanService.exportLoans(keyword, loanType, status, response);
    }
}
