package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdDataSource;
import com.apple.chain.bigdata.mapper.BdDataSourceMapper;
import com.apple.chain.bigdata.service.BdDataSourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BdDataSourceServiceImpl
        extends ServiceImpl<BdDataSourceMapper, BdDataSource>
        implements BdDataSourceService {

    @Override
    public String testConnection(Long id) {
        BdDataSource ds = getById(id);
        if (ds == null) {
            return "NOT_FOUND";
        }
        // M2 skeleton: real probe (JDBC / HTTP / RSS) wired in M3.
        String result = "OK · stub (M3 will dispatch by source_type)";
        ds.setLastTestTime(LocalDateTime.now());
        ds.setLastTestResult(result);
        updateById(ds);
        return result;
    }
}
