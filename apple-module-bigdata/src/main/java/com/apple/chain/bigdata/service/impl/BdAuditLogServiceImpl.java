package com.apple.chain.bigdata.service.impl;

import com.apple.chain.bigdata.entity.BdAuditLog;
import com.apple.chain.bigdata.mapper.BdAuditLogMapper;
import com.apple.chain.bigdata.service.BdAuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BdAuditLogServiceImpl
        extends ServiceImpl<BdAuditLogMapper, BdAuditLog>
        implements BdAuditLogService {

    @Override
    public IPage<BdAuditLog> query(String username, String module, String action, int page, int size) {
        LambdaQueryWrapper<BdAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(BdAuditLog::getUsername, username);
        }
        if (StringUtils.hasText(module)) {
            wrapper.eq(BdAuditLog::getModule, module);
        }
        if (StringUtils.hasText(action)) {
            wrapper.eq(BdAuditLog::getAction, action);
        }
        wrapper.orderByDesc(BdAuditLog::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}
