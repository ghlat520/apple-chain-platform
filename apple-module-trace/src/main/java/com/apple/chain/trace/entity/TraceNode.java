package com.apple.chain.trace.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A single node in the trace timeline.
 * Table: tr_trace_node
 */
@Getter
@Setter
@TableName("tr_trace_node")
public class TraceNode extends BaseEntity {

    private String traceCode;

    /** PLANT / GROW / HARVEST / STORAGE / LOGISTICS / TRADE */
    private String nodeType;

    private LocalDateTime nodeTime;
    private Long operatorId;
    private String operatorName;

    /** Brief summary for display */
    private String summary;

    /** JSON detail payload */
    private String detail;

    private String location;

    /** SHA-256 hash of node content */
    private String dataHash;
}
