package com.apple.chain.input.service;

import com.apple.chain.input.entity.AgriUsage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * Agricultural input usage service interface.
 */
public interface AgriUsageService extends IService<AgriUsage> {

    IPage<AgriUsage> listUsages(int page, int size, String keyword, String method, Long orchardId);

    AgriUsage getDetail(Long id);

    AgriUsage createUsage(AgriUsage usage);

    AgriUsage updateUsage(Long id, AgriUsage usage);

    void deleteUsage(Long id);

    void exportUsages(String keyword, String method, Long orchardId, HttpServletResponse response);

    List<AgriUsage> listByBatchId(Long batchId);

    List<AgriUsage> listByTraceCode(String traceCode);
}
