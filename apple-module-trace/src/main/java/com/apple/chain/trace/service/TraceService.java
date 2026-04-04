package com.apple.chain.trace.service;

import com.apple.chain.trace.entity.TraceChain;
import com.apple.chain.trace.entity.TraceNode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * Trace service interface.
 */
public interface TraceService extends IService<TraceChain> {

    IPage<TraceChain> listChains(int page, int size, String keyword, String status);

    Map<String, Object> getTraceDetail(String traceCode);

    Map<String, Object> publicScan(String traceCode);

    TraceNode addNode(TraceNode node);

    void exportChains(String keyword, String status, HttpServletResponse response);
}
