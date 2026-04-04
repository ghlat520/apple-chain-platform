package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.HarvestBatch;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Harvest batch service interface.
 */
public interface HarvestBatchService extends IService<HarvestBatch> {

    IPage<HarvestBatch> listBatches(int page, int size, Long orchardId, String status);

    HarvestBatch getBatchDetail(Long id);

    HarvestBatch createBatch(HarvestBatch batch);

    HarvestBatch updateBatch(Long id, HarvestBatch batch);

    HarvestBatch confirmBatch(Long id);

    void deleteBatch(Long id);

    void exportBatches(Long orchardId, String status, HttpServletResponse response);
}
