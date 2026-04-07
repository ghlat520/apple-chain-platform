package com.apple.chain.trace.entity;

import com.apple.chain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * Three-level traceability code (一果一码 — M12).
 * Table: trace_code
 * <p>
 * Granularity:
 *   - BATCH : identical to TraceBatch.batchCode (record is optional — the BATCH
 *             level can be looked up directly from trace_batch and is only
 *             materialized here for uniform query).
 *   - BOX   : {batchCode}-B{###}-{CRC4}
 *   - FRUIT : {boxCode}-F{####}-{CRC4}
 * <p>
 * parentCode links upward:
 *   - BATCH.parentCode   = null
 *   - BOX.parentCode     = batchCode
 *   - FRUIT.parentCode   = boxCode
 */
@Getter
@Setter
@TableName("trace_code")
public class TraceCode extends BaseEntity {

    /** The unique traceability code (UNIQUE index uk_code). */
    private String code;

    /** BATCH / BOX / FRUIT */
    private String granularity;

    /** Parent code (null for BATCH). */
    private String parentCode;

    /** FK → trace_batch.id */
    private Long batchId;

    /** CRC-16/CCITT-FALSE checksum as 4-char uppercase hex. */
    private String crc16;

    /** Public scan URL, e.g. /public/scan/{code}. */
    private String qrUrl;

    /** ACTIVE / VOID / CONSUMED */
    private String status;
}
