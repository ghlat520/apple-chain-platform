package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.CultivationOperation;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * CultivationOperation service interface.
 */
public interface CultivationOperationService extends IService<CultivationOperation> {

    IPage<CultivationOperation> listOperations(int page, int size, Long batchId, String operationType);

    CultivationOperation createOperation(CultivationOperation operation);

    CultivationOperation updateOperation(Long id, CultivationOperation operation);

    void deleteOperation(Long id);
}
