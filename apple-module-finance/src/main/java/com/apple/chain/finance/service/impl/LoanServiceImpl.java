package com.apple.chain.finance.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.finance.entity.Loan;
import com.apple.chain.finance.mapper.LoanMapper;
import com.apple.chain.finance.service.LoanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl extends ServiceImpl<LoanMapper, Loan> implements LoanService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<Loan> listLoans(int page, int size, String keyword, String loanType, String status) {
        LambdaQueryWrapper<Loan> wrapper = new LambdaQueryWrapper<Loan>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Loan::getLoanCode, keyword)
                        .or().like(Loan::getBorrowerName, keyword))
                .eq(StringUtils.hasText(loanType), Loan::getLoanType, loanType)
                .eq(StringUtils.hasText(status), Loan::getStatus, status)
                .orderByDesc(Loan::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Loan getLoanDetail(Long id) {
        Loan loan = getById(id);
        if (loan == null) throw new BizException(ResultCode.NOT_FOUND, "贷款记录不存在");
        return loan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan createLoan(Loan loan) {
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = baseMapper.nextSeq(prefix);
        loan.setLoanCode(String.format("LN%s%04d", prefix, seq));
        loan.setStatus("PENDING");
        loan.setRepaidAmount(java.math.BigDecimal.ZERO);
        save(loan);
        return loan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan updateLoan(Long id, Loan loan) {
        Loan existing = getLoanDetail(id);
        if ("DISBURSED".equals(existing.getStatus()) || "REPAID".equals(existing.getStatus())) {
            throw new BizException("已放款或已还清的贷款不允许修改");
        }
        loan.setId(id);
        loan.setLoanCode(null);
        updateById(loan);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan approve(Long id) {
        Loan loan = getLoanDetail(id);
        if (!"PENDING".equals(loan.getStatus())) throw new BizException("只有待审批的贷款可以审批");
        Loan update = new Loan();
        update.setId(id);
        update.setStatus("APPROVED");
        update.setApproveDate(LocalDate.now());
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan reject(Long id) {
        Loan loan = getLoanDetail(id);
        if (!"PENDING".equals(loan.getStatus())) throw new BizException("只有待审批的贷款可以拒绝");
        Loan update = new Loan();
        update.setId(id);
        update.setStatus("REJECTED");
        update.setApproveDate(LocalDate.now());
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan disburse(Long id) {
        Loan loan = getLoanDetail(id);
        if (!"APPROVED".equals(loan.getStatus())) throw new BizException("只有已审批的贷款可以放款");
        Loan update = new Loan();
        update.setId(id);
        update.setStatus("DISBURSED");
        update.setDisburseDate(LocalDate.now());
        update.setDueDate(LocalDate.now().plusMonths(loan.getTermMonths()));
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoan(Long id) {
        Loan loan = getLoanDetail(id);
        if ("DISBURSED".equals(loan.getStatus())) throw new BizException("已放款的贷款不能删除");
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan applyPlantLoan(Loan loan) {
        loan.setLoanType("PLANT");
        loan.setStatus("PENDING");
        return createLoan(loan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan applyWarehouseLoan(Loan loan) {
        loan.setLoanType("WAREHOUSE");
        loan.setStatus("PENDING");
        return createLoan(loan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan applyTradeLoan(Loan loan) {
        loan.setLoanType("TRADE");
        loan.setStatus("PENDING");
        return createLoan(loan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan applyExportLoan(Loan loan) {
        loan.setLoanType("EXPORT");
        loan.setStatus("PENDING");
        return createLoan(loan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan repay(Long id, java.math.BigDecimal amount) {
        Loan loan = getLoanDetail(id);
        java.math.BigDecimal repaid = loan.getRepaidAmount() == null
                ? java.math.BigDecimal.ZERO : loan.getRepaidAmount();
        java.math.BigDecimal newRepaid = repaid.add(amount);
        Loan update = new Loan();
        update.setId(id);
        update.setRepaidAmount(newRepaid);
        if (newRepaid.compareTo(loan.getAmount()) >= 0) {
            update.setStatus("REPAID");
        }
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan settle(Long id) {
        Loan loan = getLoanDetail(id);
        if (!"REPAID".equals(loan.getStatus())) throw new BizException("只有已还清的贷款可以结清");
        Loan update = new Loan();
        update.setId(id);
        update.setStatus("SETTLED");
        updateById(update);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Loan markOverdue(Long id) {
        Loan loan = getLoanDetail(id);
        if (!"DISBURSED".equals(loan.getStatus())) throw new BizException("只有已放款的贷款可以标记为逾期");
        Loan update = new Loan();
        update.setId(id);
        update.setStatus("OVERDUE");
        updateById(update);
        return getById(id);
    }

    @Override
    public void exportLoans(String keyword, String loanType, String status, HttpServletResponse response) {
        List<Loan> list = list(new LambdaQueryWrapper<Loan>()
                .and(StringUtils.hasText(keyword), w -> w.like(Loan::getLoanCode, keyword).or().like(Loan::getBorrowerName, keyword))
                .eq(StringUtils.hasText(loanType), Loan::getLoanType, loanType)
                .eq(StringUtils.hasText(status), Loan::getStatus, status));
        try {
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("贷款记录.csv", StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF');
            writer.println("贷款编号,借款人,类型,贷款类型,金额,利率,期限(月),申请日期,审批日期,放款日期,到期日期,已还金额,状态");
            for (Loan l : list) {
                writer.println(l.getLoanCode() + "," + l.getBorrowerName() + "," + l.getBorrowerType() + "," +
                        l.getLoanType() + "," + l.getAmount() + "," + l.getInterestRate() + "," +
                        l.getTermMonths() + "," + l.getApplyDate() + "," + l.getApproveDate() + "," +
                        l.getDisburseDate() + "," + l.getDueDate() + "," + l.getRepaidAmount() + "," + l.getStatus());
            }
            writer.flush();
        } catch (Exception e) { throw new BizException("导出失败: " + e.getMessage()); }
    }
}
