package com.apple.chain.finance.service;

import com.apple.chain.finance.dto.FinanceStatisticsVO;

public interface FinanceStatisticsService {
    FinanceStatisticsVO getSummary();
    FinanceStatisticsVO getRiskSummary();
}
