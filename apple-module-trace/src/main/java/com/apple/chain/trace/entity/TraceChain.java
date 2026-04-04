package com.apple.chain.trace.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Trace chain - the top-level traceability record for a harvest batch.
 * Table: tr_trace_chain
 */
@Getter
@Setter
@TableName("tr_trace_chain")
public class TraceChain extends BaseEntity {

    /** Unique trace code, e.g. TCHB20250101001 */
    private String traceCode;

    private String productType;

    /** Reference to pt_harvest_batch.batch_no */
    private String batchNo;

    private Long orchardId;
    private Long farmerId;

    /** PLANTED / HARVESTED / IN_STORAGE / IN_TRANSIT / SOLD */
    private String currentStatus;

    /** SHA-256 hash of key fields for integrity verification */
    private String dataHash;

    /** 0=pending, 1=chained (on blockchain) */
    private Integer chainStatus;

    private String remark;
}
