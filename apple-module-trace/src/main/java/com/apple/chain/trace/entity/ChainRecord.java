package com.apple.chain.trace.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * M2 奥链上链记录. Table: tr_chain_record (V16).
 * Each row = one (asynchronous) attempt to push a business snapshot to chain.
 */
@Getter
@Setter
@TableName("tr_chain_record")
public class ChainRecord extends BaseEntity {

    /** Trace code being submitted (BATCH / BOX / FRUIT). */
    private String traceCode;

    /** Business kind: BATCH / BOX / FRUIT (later: cultivation / harvest / trade / ...). */
    private String businessType;

    /** Optional business primary key (e.g. trace_code.id). */
    private Long businessId;

    /** Canonical JSON snapshot at upload time (forensic record). */
    private String dataSnapshot;

    /** SHA-256 hex of dataSnapshot. */
    private String dataHash;

    /** Chain tx hash returned by 奥链 (null until success). */
    private String chainTxHash;

    /** Chain block height (null until success). */
    private Long chainBlockHeight;

    /** 0=pending, 1=success, 2=failed (terminal), 3=retrying. */
    private Integer chainStatus;

    /** Last failure message (truncated to 512 chars). */
    private String errorMsg;

    /** Number of retries attempted so far. */
    private Integer retryCount;

    // ==== status constants ====
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED  = 2;
    public static final int STATUS_RETRY   = 3;

    public static final int MAX_RETRY = 3;
}
