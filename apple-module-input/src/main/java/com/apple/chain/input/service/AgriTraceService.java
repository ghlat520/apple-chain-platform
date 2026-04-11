package com.apple.chain.input.service;

import com.apple.chain.input.dto.AgriTraceChainVO;

/**
 * Agricultural input traceability chain service interface.
 */
public interface AgriTraceService {

    /**
     * Query full traceability chain by traceCode:
     * usage records → product → purchase records → supplier info.
     */
    AgriTraceChainVO getTraceChain(String traceCode);
}
