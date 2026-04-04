package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.CultivationBatch;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CultivationBatch service interface.
 */
public interface CultivationBatchService extends IService<CultivationBatch> {

    IPage<CultivationBatch> listBatches(int page, int size, String keyword, String status, Long orchardId);

    CultivationBatch getBatchDetail(Long id);

    CultivationBatch createBatch(CultivationBatch batch);

    CultivationBatch updateBatch(Long id, CultivationBatch batch);

    void deleteBatch(Long id);

    void exportBatches(String keyword, String status, Long orchardId, HttpServletResponse response);
}
