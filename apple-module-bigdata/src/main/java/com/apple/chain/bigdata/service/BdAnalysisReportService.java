package com.apple.chain.bigdata.service;

import com.apple.chain.bigdata.entity.BdAnalysisReport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Analysis report service.
 */
public interface BdAnalysisReportService extends IService<BdAnalysisReport> {

    /**
     * Generate a new analysis report by aggregating data from planting/trade/warehouse/finance tables.
     *
     * @param type   WEEKLY / MONTHLY / SEASONAL
     * @param period e.g. 2026-04
     * @return the generated report (DRAFT status)
     */
    BdAnalysisReport generate(String type, String period);

    /**
     * Paginated list of reports.
     */
    IPage<BdAnalysisReport> list(int page, int size);

    /**
     * Get report detail by id.
     */
    BdAnalysisReport getDetail(Long id);

    /**
     * Publish a DRAFT report (set status to PUBLISHED).
     */
    void publish(Long id);
}
