package com.apple.chain.finance;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.finance.entity.Loan;
import com.apple.chain.finance.mapper.LoanMapper;
import com.apple.chain.finance.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * M7 LoanService unit tests.
 *
 * Tests the full loan lifecycle state machine:
 *   PENDING -> APPROVED -> DISBURSED -> REPAID -> SETTLED
 *   PENDING -> REJECTED
 *   DISBURSED -> OVERDUE
 *
 * Invalid transitions throw BizException.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService - 贷款服务")
class LoanServiceTest {

    @Mock
    private LoanMapper loanMapper;

    private LoanServiceImpl loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanServiceImpl();
        ReflectionTestUtils.setField(loanService, "baseMapper", loanMapper);
    }

    // ── Helper ──────────────────────────────────────────────────────

    private Loan makeLoan(Long id, String status, BigDecimal amount, int termMonths) {
        Loan loan = new Loan();
        loan.setId(id);
        loan.setLoanCode("LN202604010001");
        loan.setBorrowerName("Test Farm");
        loan.setBorrowerType("ENTERPRISE");
        loan.setBorrowerId(10L);
        loan.setAmount(amount);
        loan.setInterestRate(new BigDecimal("4.5"));
        loan.setTermMonths(termMonths);
        loan.setStatus(status);
        loan.setRepaidAmount(BigDecimal.ZERO);
        loan.setApplyDate(LocalDate.now());
        return loan;
    }

    // ── Apply ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("applyXxxLoan - 申请各类型贷款")
    class ApplyLoan {

        @Test
        @DisplayName("applyPlantLoan 设置 PLANT 类型")
        void plant_loan_type() {
            when(loanMapper.nextSeq(anyString())).thenReturn(1);
            when(loanMapper.insert(any(Loan.class))).thenReturn(1);
            Loan input = makeLoan(null, null, new BigDecimal("100000"), 12);

            Loan result = loanService.applyPlantLoan(input);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).insert(captor.capture());
            Loan saved = captor.getValue();
            assertThat(saved.getLoanType()).isEqualTo("PLANT");
            assertThat(saved.getStatus()).isEqualTo("PENDING");
            assertThat(saved.getLoanCode()).startsWith("LN");
            assertThat(saved.getRepaidAmount()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("applyWarehouseLoan 设置 WAREHOUSE 类型")
        void warehouse_loan_type() {
            when(loanMapper.nextSeq(anyString())).thenReturn(1);
            when(loanMapper.insert(any(Loan.class))).thenReturn(1);
            Loan input = makeLoan(null, null, new BigDecimal("50000"), 6);

            loanService.applyWarehouseLoan(input);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).insert(captor.capture());
            assertThat(captor.getValue().getLoanType()).isEqualTo("WAREHOUSE");
        }

        @Test
        @DisplayName("applyTradeLoan 设置 TRADE 类型")
        void trade_loan_type() {
            when(loanMapper.nextSeq(anyString())).thenReturn(1);
            when(loanMapper.insert(any(Loan.class))).thenReturn(1);
            Loan input = makeLoan(null, null, new BigDecimal("200000"), 12);

            loanService.applyTradeLoan(input);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).insert(captor.capture());
            assertThat(captor.getValue().getLoanType()).isEqualTo("TRADE");
        }

        @Test
        @DisplayName("applyExportLoan 设置 EXPORT 类型")
        void export_loan_type() {
            when(loanMapper.nextSeq(anyString())).thenReturn(1);
            when(loanMapper.insert(any(Loan.class))).thenReturn(1);
            Loan input = makeLoan(null, null, new BigDecimal("300000"), 24);

            loanService.applyExportLoan(input);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).insert(captor.capture());
            assertThat(captor.getValue().getLoanType()).isEqualTo("EXPORT");
        }

        @Test
        @DisplayName("贷款编号按日期+序号生成")
        void loan_code_format() {
            when(loanMapper.nextSeq(anyString())).thenReturn(42);
            when(loanMapper.insert(any(Loan.class))).thenReturn(1);
            Loan input = makeLoan(null, null, new BigDecimal("10000"), 6);

            loanService.applyPlantLoan(input);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).insert(captor.capture());
            String code = captor.getValue().getLoanCode();
            assertThat(code).startsWith("LN");
            assertThat(code).endsWith("0042");
        }
    }

    // ── State Machine: PENDING -> APPROVED -> DISBURSED ────────────

    @Nested
    @DisplayName("approve / reject - 审批状态转换")
    class ApproveReject {

        @Test
        @DisplayName("PENDING 贷款可以审批通过")
        void approve_pending_loan() {
            Loan loan = makeLoan(1L, "PENDING", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            Loan result = loanService.approve(1L);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("APPROVED");
            assertThat(captor.getValue().getApproveDate()).isNotNull();
        }

        @Test
        @DisplayName("PENDING 贷款可以拒绝")
        void reject_pending_loan() {
            Loan loan = makeLoan(1L, "PENDING", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.reject(1L);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("REJECTED");
        }

        @Test
        @DisplayName("非 PENDING 贷款不能审批")
        void cannot_approve_non_pending() {
            Loan loan = makeLoan(1L, "APPROVED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.approve(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待审批的贷款可以审批");
        }

        @Test
        @DisplayName("非 PENDING 贷款不能拒绝")
        void cannot_reject_non_pending() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.reject(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待审批的贷款可以拒绝");
        }

        @Test
        @DisplayName("审批不存在的贷款抛出异常")
        void approve_nonexistent_loan() {
            when(loanMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> loanService.approve(999L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("贷款记录不存在");
        }
    }

    // ── State Machine: APPROVED -> DISBURSED ────────────────────────

    @Nested
    @DisplayName("disburse - 放款状态转换")
    class Disburse {

        @Test
        @DisplayName("APPROVED 贷款可以放款")
        void disburse_approved_loan() {
            Loan loan = makeLoan(1L, "APPROVED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.disburse(1L);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("DISBURSED");
            assertThat(captor.getValue().getDisburseDate()).isNotNull();
            assertThat(captor.getValue().getDueDate())
                    .isNotNull()
                    .isEqualTo(captor.getValue().getDisburseDate().plusMonths(12));
        }

        @Test
        @DisplayName("非 APPROVED 贷款不能放款")
        void cannot_disburse_non_approved() {
            Loan loan = makeLoan(1L, "PENDING", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.disburse(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有已审批的贷款可以放款");
        }
    }

    // ── State Machine: DISBURSED -> REPAID -> SETTLED ───────────────

    @Nested
    @DisplayName("repay / settle - 还款与结清")
    class RepaySettle {

        @Test
        @DisplayName("部分还款不改变状态")
        void partial_repay_keeps_status() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            loan.setRepaidAmount(new BigDecimal("30000"));
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.repay(1L, new BigDecimal("20000"));

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getRepaidAmount())
                    .isEqualByComparingTo("50000");
        }

        @Test
        @DisplayName("全额还款自动变为 REPAID")
        void full_repay_changes_to_repaid() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            loan.setRepaidAmount(new BigDecimal("80000"));
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.repay(1L, new BigDecimal("20000"));

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("REPAID");
            assertThat(captor.getValue().getRepaidAmount())
                    .isEqualByComparingTo("100000");
        }

        @Test
        @DisplayName("超额还款也变为 REPAID")
        void overpay_changes_to_repaid() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            loan.setRepaidAmount(new BigDecimal("80000"));
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.repay(1L, new BigDecimal("30000"));

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("REPAID");
        }

        @Test
        @DisplayName("repaidAmount 为 null 时从零开始计算")
        void null_repaid_starts_from_zero() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            loan.setRepaidAmount(null);
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.repay(1L, new BigDecimal("100000"));

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getRepaidAmount())
                    .isEqualByComparingTo("100000");
            assertThat(captor.getValue().getStatus()).isEqualTo("REPAID");
        }

        @Test
        @DisplayName("REPAID 贷款可以结清")
        void settle_repaid_loan() {
            Loan loan = makeLoan(1L, "REPAID", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.settle(1L);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("SETTLED");
        }

        @Test
        @DisplayName("非 REPAID 贷款不能结清")
        void cannot_settle_non_repaid() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.settle(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有已还清的贷款可以结清");
        }
    }

    // ── State Machine: DISBURSED -> OVERDUE ─────────────────────────

    @Nested
    @DisplayName("markOverdue - 标记逾期")
    class MarkOverdue {

        @Test
        @DisplayName("DISBURSED 贷款可以标记逾期")
        void mark_disbursed_as_overdue() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            loanService.markOverdue(1L);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("OVERDUE");
        }

        @Test
        @DisplayName("非 DISBURSED 贷款不能标记逾期")
        void cannot_mark_non_disbursed_overdue() {
            Loan loan = makeLoan(1L, "PENDING", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.markOverdue(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有已放款的贷款可以标记为逾期");
        }

        @Test
        @DisplayName("REPAID 贷款不能标记逾期")
        void cannot_mark_repaid_overdue() {
            Loan loan = makeLoan(1L, "REPAID", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.markOverdue(1L))
                    .isInstanceOf(BizException.class);
        }
    }

    // ── Update & Delete guards ──────────────────────────────────────

    @Nested
    @DisplayName("updateLoan / deleteLoan - 修改与删除守卫")
    class UpdateDelete {

        @Test
        @DisplayName("DISBURSED 贷款不允许修改")
        void cannot_update_disbursed() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            Loan update = new Loan();
            assertThatThrownBy(() -> loanService.updateLoan(1L, update))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已放款或已还清的贷款不允许修改");
        }

        @Test
        @DisplayName("REPAID 贷款不允许修改")
        void cannot_update_repaid() {
            Loan loan = makeLoan(1L, "REPAID", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            Loan update = new Loan();
            assertThatThrownBy(() -> loanService.updateLoan(1L, update))
                    .isInstanceOf(BizException.class);
        }

        @Test
        @DisplayName("DISBURSED 贷款不能删除")
        void cannot_delete_disbursed() {
            Loan loan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan);

            assertThatThrownBy(() -> loanService.deleteLoan(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已放款的贷款不能删除");
        }

        @Test
        @DisplayName("修改时清除 loanCode 防止覆盖")
        void update_clears_loan_code() {
            Loan loan = makeLoan(1L, "PENDING", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(loan, loan);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);
            Loan update = new Loan();
            update.setLoanCode("SHOULD_BE_CLEARED");
            update.setAmount(new BigDecimal("200000"));

            loanService.updateLoan(1L, update);

            ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).updateById(captor.capture());
            assertThat(captor.getValue().getLoanCode()).isNull();
        }
    }

    // ── Full lifecycle ──────────────────────────────────────────────

    @Nested
    @DisplayName("完整生命周期: 申请 -> 审批 -> 放款 -> 还款 -> 结清")
    class FullLifecycle {

        @Test
        @DisplayName("正常全流程状态流转")
        void happy_path_lifecycle() {
            when(loanMapper.nextSeq(anyString())).thenReturn(1);
            when(loanMapper.insert(any(Loan.class))).thenReturn(1);
            when(loanMapper.updateById(any(Loan.class))).thenReturn(1);

            // 1. Apply - verify via captor
            Loan input = makeLoan(null, null, new BigDecimal("100000"), 12);
            loanService.applyPlantLoan(input);
            ArgumentCaptor<Loan> applyCaptor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper).insert(applyCaptor.capture());
            assertThat(applyCaptor.getValue().getLoanType()).isEqualTo("PLANT");
            assertThat(applyCaptor.getValue().getStatus()).isEqualTo("PENDING");

            // 2. Approve - mock selectById to return PENDING, then APPROVED
            Loan pending = makeLoan(1L, "PENDING", new BigDecimal("100000"), 12);
            Loan afterApprove = makeLoan(1L, "APPROVED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(pending, afterApprove);
            loanService.approve(1L);
            ArgumentCaptor<Loan> approveCaptor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper, atLeastOnce()).updateById(approveCaptor.capture());
            // find the approve update
            Loan approveUpdate = approveCaptor.getAllValues().stream()
                    .filter(l -> "APPROVED".equals(l.getStatus()))
                    .findFirst().orElse(null);
            assertThat(approveUpdate).isNotNull();
            assertThat(approveUpdate.getApproveDate()).isNotNull();

            // 3. Disburse - mock selectById to return APPROVED, then DISBURSED
            Loan approvedLoan = makeLoan(1L, "APPROVED", new BigDecimal("100000"), 12);
            Loan afterDisburse = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(approvedLoan, afterDisburse);
            loanService.disburse(1L);
            ArgumentCaptor<Loan> disburseCaptor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper, atLeastOnce()).updateById(disburseCaptor.capture());
            Loan disburseUpdate = disburseCaptor.getAllValues().stream()
                    .filter(l -> "DISBURSED".equals(l.getStatus()))
                    .findFirst().orElse(null);
            assertThat(disburseUpdate).isNotNull();
            assertThat(disburseUpdate.getDisburseDate()).isNotNull();

            // 4. Repay (full) - mock selectById to return DISBURSED, then REPAID
            Loan disbursedLoan = makeLoan(1L, "DISBURSED", new BigDecimal("100000"), 12);
            disbursedLoan.setRepaidAmount(BigDecimal.ZERO);
            Loan afterRepay = makeLoan(1L, "REPAID", new BigDecimal("100000"), 12);
            afterRepay.setRepaidAmount(new BigDecimal("100000"));
            when(loanMapper.selectById(1L)).thenReturn(disbursedLoan, afterRepay);
            loanService.repay(1L, new BigDecimal("100000"));
            ArgumentCaptor<Loan> repayCaptor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper, atLeastOnce()).updateById(repayCaptor.capture());
            Loan repayUpdate = repayCaptor.getAllValues().stream()
                    .filter(l -> l.getStatus() != null && l.getStatus().equals("REPAID"))
                    .findFirst().orElse(null);
            assertThat(repayUpdate).isNotNull();

            // 5. Settle - mock selectById to return REPAID, then SETTLED
            Loan repaidLoan = makeLoan(1L, "REPAID", new BigDecimal("100000"), 12);
            Loan afterSettle = makeLoan(1L, "SETTLED", new BigDecimal("100000"), 12);
            when(loanMapper.selectById(1L)).thenReturn(repaidLoan, afterSettle);
            loanService.settle(1L);
            ArgumentCaptor<Loan> settleCaptor = ArgumentCaptor.forClass(Loan.class);
            verify(loanMapper, atLeastOnce()).updateById(settleCaptor.capture());
            Loan settleUpdate = settleCaptor.getAllValues().stream()
                    .filter(l -> "SETTLED".equals(l.getStatus()))
                    .findFirst().orElse(null);
            assertThat(settleUpdate).isNotNull();
        }
    }
}
