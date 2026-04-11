package com.apple.chain.input.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.input.dto.AgriTraceChainVO;
import com.apple.chain.input.entity.AgriPurchase;
import com.apple.chain.input.entity.AgriSupplier;
import com.apple.chain.input.entity.AgriUsage;
import com.apple.chain.input.mapper.AgriPurchaseMapper;
import com.apple.chain.input.mapper.AgriSupplierMapper;
import com.apple.chain.input.mapper.AgriUsageMapper;
import com.apple.chain.input.service.AgriTraceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Traceability chain service implementation.
 * Chain: traceCode → usage records → productId → purchase records → supplierId → supplier info.
 */
@Service
@RequiredArgsConstructor
public class AgriTraceServiceImpl implements AgriTraceService {

    private final AgriUsageMapper agriUsageMapper;
    private final AgriPurchaseMapper agriPurchaseMapper;
    private final AgriSupplierMapper agriSupplierMapper;

    @Override
    public AgriTraceChainVO getTraceChain(String traceCode) {
        // 1. query usages by traceCode
        List<AgriUsage> usages = agriUsageMapper.selectList(
                new LambdaQueryWrapper<AgriUsage>()
                        .eq(AgriUsage::getTraceCode, traceCode)
                        .orderByDesc(AgriUsage::getUsageDate));

        if (usages.isEmpty()) {
            throw new BizException(ResultCode.NOT_FOUND, "溯源码对应的使用记录不存在: " + traceCode);
        }

        // 2. collect productIds from usages
        Set<Long> productIds = usages.stream()
                .map(AgriUsage::getProductId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 3. query purchases for those products
        List<AgriPurchase> purchases = new ArrayList<>();
        if (!productIds.isEmpty()) {
            purchases = agriPurchaseMapper.selectList(
                    new LambdaQueryWrapper<AgriPurchase>()
                            .in(AgriPurchase::getProductId, productIds)
                            .orderByDesc(AgriPurchase::getPurchaseDate));
        }

        // 4. collect supplierIds from purchases
        Set<Long> supplierIds = purchases.stream()
                .map(AgriPurchase::getSupplierId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 5. query suppliers
        List<AgriSupplier> suppliers = new ArrayList<>();
        if (!supplierIds.isEmpty()) {
            suppliers = agriSupplierMapper.selectList(
                    new LambdaQueryWrapper<AgriSupplier>()
                            .in(AgriSupplier::getId, supplierIds));
        }

        AgriTraceChainVO vo = new AgriTraceChainVO();
        vo.setTraceCode(traceCode);
        vo.setUsages(usages);
        vo.setPurchases(purchases);
        vo.setSuppliers(suppliers);
        return vo;
    }
}
