package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdDataLineage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/** Data lineage graph. */
public interface BdDataLineageService extends IService<BdDataLineage> {

    /** All edges where (nodeType, nodeId) is the upstream. */
    List<BdDataLineage> findDownstream(String nodeType, String nodeId);

    /** All edges where (nodeType, nodeId) is the downstream. */
    List<BdDataLineage> findUpstream(String nodeType, String nodeId);
}
