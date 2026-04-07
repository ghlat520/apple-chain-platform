package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdDataSource;
import com.baomidou.mybatisplus.extension.service.IService;

/** Data-source registry service. */
public interface BdDataSourceService extends IService<BdDataSource> {

    /** Attempt to connect to the source; stores result in last_test_*. */
    String testConnection(Long id);
}
