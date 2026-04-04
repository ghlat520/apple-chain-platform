package com.apple.chain.finance.service;

import com.apple.chain.finance.entity.RiskRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

public interface RiskRecordService extends IService<RiskRecord> {
    IPage<RiskRecord> listRecords(int page, int size, String keyword, String riskLevel, String status);
    RiskRecord getRecordDetail(Long id);
    RiskRecord createRecord(RiskRecord record);
    RiskRecord updateRecord(Long id, RiskRecord record);
    void deleteRecord(Long id);
    void exportRecords(String keyword, String riskLevel, String status, HttpServletResponse response);
}
