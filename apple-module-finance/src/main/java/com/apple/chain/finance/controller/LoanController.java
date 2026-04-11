package com.apple.chain.finance.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.finance.entity.Loan;
import com.apple.chain.finance.service.LoanService;
import com.apple.chain.finance.service.LoanWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "贷款管理")
@RestController
@RequestMapping("/api/finance/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final LoanWorkflowService loanWorkflowService;

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

    @Operation(summary = "申请种植贷款")
    @PostMapping("/apply/plant")
    public R<Loan> applyPlant(@RequestBody Loan loan) { return R.ok(loanService.applyPlantLoan(loan)); }

    @Operation(summary = "申请仓单贷款")
    @PostMapping("/apply/warehouse")
    public R<Loan> applyWarehouse(@RequestBody Loan loan) { return R.ok(loanService.applyWarehouseLoan(loan)); }

    @Operation(summary = "申请贸易贷款")
    @PostMapping("/apply/trade")
    public R<Loan> applyTrade(@RequestBody Loan loan) { return R.ok(loanService.applyTradeLoan(loan)); }

    @Operation(summary = "申请出口贷款")
    @PostMapping("/apply/export")
    public R<Loan> applyExport(@RequestBody Loan loan) { return R.ok(loanService.applyExportLoan(loan)); }

    @Operation(summary = "还款")
    @PostMapping("/{id}/repay")
    public R<Loan> repay(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return R.ok(loanService.repay(id, amount));
    }

    @Operation(summary = "结清贷款")
    @PostMapping("/{id}/settle")
    public R<Loan> settle(@PathVariable Long id) { return R.ok(loanService.settle(id)); }

    @Operation(summary = "标记逾期")
    @PostMapping("/{id}/overdue")
    public R<Loan> markOverdue(@PathVariable Long id) { return R.ok(loanService.markOverdue(id)); }

    @Operation(summary = "贷款工作流（合同+发票+支付）")
    @PostMapping("/{id}/process-workflow")
    public R<Void> processWorkflow(@PathVariable Long id) {
        loanWorkflowService.processLoanWithContract(id);
        return R.ok("工作流处理成功", null);
    }

    @Operation(summary = "导出贷款CSV")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String keyword, @RequestParam(required = false) String loanType,
                       @RequestParam(required = false) String status, HttpServletResponse response) {
        loanService.exportLoans(keyword, loanType, status, response);
    }
}
