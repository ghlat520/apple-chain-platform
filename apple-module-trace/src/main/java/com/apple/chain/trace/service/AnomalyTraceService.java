package com.apple.chain.trace.service;

import com.apple.chain.trace.entity.AnomalyTrace;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface AnomalyTraceService extends IService<AnomalyTrace> {

    IPage<AnomalyTrace> listAnomalies(int page, int size, String type, String status, String severity);

    AnomalyTrace getAnomalyDetail(Long id);

    AnomalyTrace reportAnomaly(AnomalyTrace anomaly);

    AnomalyTrace investigate(Long id);

    AnomalyTrace resolve(Long id, String rootCause, String resolvedBy);

    Map<String, Object> impactAnalysis(String traceCode);
}
