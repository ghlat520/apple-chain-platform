package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdApiCallLog;
import com.apple.chain.bigdata.entity.BdApiClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/** Open API client + call log. */
public interface BdApiClientService extends IService<BdApiClient> {

    IPage<BdApiCallLog> listCallLogs(String appKey, int page, int size);
}
