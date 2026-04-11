package com.apple.chain.trace.service;

import com.apple.chain.trace.dto.TraceFullChainVO;

public interface TraceAggregationService {

    TraceFullChainVO getFullChain(String traceCode);
}
