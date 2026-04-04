package com.apple.chain.planting.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.planting.entity.CultivationOperation;
import com.apple.chain.planting.mapper.CultivationOperationMapper;
import com.apple.chain.planting.service.CultivationOperationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CultivationOperation service implementation.
 */
@Service
public class CultivationOperationServiceImpl extends ServiceImpl<CultivationOperationMapper, CultivationOperation>
        implements CultivationOperationService {

    @Override
    public IPage<CultivationOperation> listOperations(int page, int size, Long batchId, String operationType) {
        LambdaQueryWrapper<CultivationOperation> wrapper = new LambdaQueryWrapper<CultivationOperation>()
                .eq(batchId != null, CultivationOperation::getBatchId, batchId)
                .eq(operationType != null && !operationType.isBlank(),
                        CultivationOperation::getOperationType, operationType)
                .orderByDesc(CultivationOperation::getOperationDate);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CultivationOperation createOperation(CultivationOperation operation) {
        save(operation);
        return operation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CultivationOperation updateOperation(Long id, CultivationOperation operation) {
        if (getById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农事操作记录不存在");
        }
        operation.setId(id);
        updateById(operation);
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOperation(Long id) {
        if (!removeById(id)) {
            throw new BizException(ResultCode.NOT_FOUND, "农事操作记录不存在");
        }
    }
}
