package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdDataLineage;
import com.apple.chain.bigdata.mapper.BdDataLineageMapper;
import com.apple.chain.bigdata.service.BdDataLineageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BdDataLineageServiceImpl
        extends ServiceImpl<BdDataLineageMapper, BdDataLineage>
        implements BdDataLineageService {

    @Override
    public List<BdDataLineage> findDownstream(String nodeType, String nodeId) {
        return list(new LambdaQueryWrapper<BdDataLineage>()
                .eq(BdDataLineage::getUpstreamType, nodeType)
                .eq(BdDataLineage::getUpstreamId, nodeId));
    }

    @Override
    public List<BdDataLineage> findUpstream(String nodeType, String nodeId) {
        return list(new LambdaQueryWrapper<BdDataLineage>()
                .eq(BdDataLineage::getDownstreamType, nodeType)
                .eq(BdDataLineage::getDownstreamId, nodeId));
    }
}
