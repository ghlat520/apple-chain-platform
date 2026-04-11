package com.apple.chain.finance.service.impl;

import com.apple.chain.finance.dto.FinanceStatisticsVO;
import com.apple.chain.finance.entity.CreditRating;
import com.apple.chain.finance.entity.Loan;
import com.apple.chain.finance.mapper.CreditRatingMapper;
import com.apple.chain.finance.mapper.LoanMapper;
import com.apple.chain.finance.service.FinanceStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceStatisticsServiceImpl implements FinanceStatisticsService {

    private final LoanMapper loanMapper;
    private final CreditRatingMapper creditRatingMapper;

    @Override
    public FinanceStatisticsVO getSummary() {
        List<Loan> loans = loanMapper.selectList(new LambdaQueryWrapper<>());
        long totalLoans = loans.size();
        BigDecimal totalAmount = loans.stream()
                .map(l -> l.getAmount() != null ? l.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> loansByType = loans.stream()
                .filter(l -> l.getLoanType() != null)
                .collect(Collectors.groupingBy(Loan::getLoanType, Collectors.counting()));
        Map<String, Long> loansByStatus = loans.stream()
                .filter(l -> l.getStatus() != null)
                .collect(Collectors.groupingBy(Loan::getStatus, Collectors.counting()));

        return FinanceStatisticsVO.builder()
                .totalLoans(totalLoans)
                .totalAmount(totalAmount)
                .loansByType(loansByType)
                .loansByStatus(loansByStatus)
                .build();
    }

    @Override
    public FinanceStatisticsVO getRiskSummary() {
        List<Loan> loans = loanMapper.selectList(new LambdaQueryWrapper<>());
        long totalLoans = loans.size();
        long overdueCount = loans.stream()
                .filter(l -> "OVERDUE".equals(l.getStatus()))
                .count();
        double overdueRate = totalLoans == 0 ? 0.0 : (double) overdueCount / totalLoans;

        List<CreditRating> ratings = creditRatingMapper.selectList(
                new LambdaQueryWrapper<CreditRating>().eq(CreditRating::getStatus, "ACTIVE"));
        Map<String, Long> riskByLevel = ratings.stream()
                .filter(r -> r.getCreditLevel() != null)
                .collect(Collectors.groupingBy(CreditRating::getCreditLevel, Collectors.counting()));

        return FinanceStatisticsVO.builder()
                .overdueRate(overdueRate)
                .riskByLevel(riskByLevel)
                .build();
    }
}
