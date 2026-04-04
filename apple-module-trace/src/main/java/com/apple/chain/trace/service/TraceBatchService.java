package com.apple.chain.trace.service;

import com.apple.chain.trace.entity.TraceBatch;
import com.apple.chain.trace.entity.TraceRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * TraceBatch service interface.
 */
public interface TraceBatchService extends IService<TraceBatch> {

    IPage<TraceBatch> listBatches(int page, int size, String keyword, String status);

    TraceBatch getBatchDetail(Long id);

    Map<String, Object> scanByBatchCode(String batchCode);

    TraceBatch createBatch(TraceBatch batch);

    TraceBatch updateStatus(Long id, String status);

    void deleteBatch(Long id);

    TraceRecord addRecord(Long batchId, TraceRecord record);

    List<TraceRecord> listRecords(Long batchId);

    void exportBatches(String keyword, String status, HttpServletResponse response);
}
