package com.apple.chain.bigdata.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Analysis report entity. Table: bd_analysis_report
 */
@Getter
@Setter
@TableName("bd_analysis_report")
public class BdAnalysisReport extends BaseEntity {

    /** Unique report number, e.g. RPT-2026-04-001 */
    private String reportNo;

    /** WEEKLY / MONTHLY / SEASONAL */
    private String reportType;

    private String title;

    /** e.g. 2026-04 for monthly, 2026-W14 for weekly */
    private String period;

    private LocalDateTime generatedTime;

    /** JSON: planting section aggregation */
    private String plantingSection;

    /** JSON: trade section aggregation */
    private String tradeSection;

    /** JSON: warehouse section aggregation */
    private String warehouseSection;

    /** JSON: finance section aggregation */
    private String financeSection;

    private String summary;

    /** DRAFT / PUBLISHED */
    private String status;
}
