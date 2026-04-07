package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdAuditLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/** Big-data admin audit log query service. */
public interface BdAuditLogService extends IService<BdAuditLog> {

    IPage<BdAuditLog> query(String username, String module, String action, int page, int size);
}
