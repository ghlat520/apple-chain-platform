package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdApiCallLog;
import com.apple.chain.bigdata.entity.BdApiClient;
import com.apple.chain.bigdata.mapper.BdApiCallLogMapper;
import com.apple.chain.bigdata.mapper.BdApiClientMapper;
import com.apple.chain.bigdata.service.BdApiClientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BdApiClientServiceImpl
        extends ServiceImpl<BdApiClientMapper, BdApiClient>
        implements BdApiClientService {

    private final BdApiCallLogMapper callLogMapper;

    @Override
    public IPage<BdApiCallLog> listCallLogs(String appKey, int page, int size) {
        LambdaQueryWrapper<BdApiCallLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(appKey)) {
            wrapper.eq(BdApiCallLog::getAppKey, appKey);
        }
        wrapper.orderByDesc(BdApiCallLog::getCreateTime);
        return callLogMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
