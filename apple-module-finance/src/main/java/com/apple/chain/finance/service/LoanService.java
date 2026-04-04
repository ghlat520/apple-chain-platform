package com.apple.chain.finance.service;

import com.apple.chain.finance.entity.Loan;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

public interface LoanService extends IService<Loan> {
    IPage<Loan> listLoans(int page, int size, String keyword, String loanType, String status);
    Loan getLoanDetail(Long id);
    Loan createLoan(Loan loan);
    Loan updateLoan(Long id, Loan loan);
    Loan approve(Long id);
    Loan reject(Long id);
    Loan disburse(Long id);
    void deleteLoan(Long id);
    void exportLoans(String keyword, String loanType, String status, HttpServletResponse response);
}
