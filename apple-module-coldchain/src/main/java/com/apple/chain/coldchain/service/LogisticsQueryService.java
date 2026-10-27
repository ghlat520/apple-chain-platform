package com.apple.chain.coldchain.service;

import com.apple.chain.coldchain.dto.LogisticsFullVO;

public interface LogisticsQueryService {

    LogisticsFullVO getFullLogistics(Long taskId);
}
