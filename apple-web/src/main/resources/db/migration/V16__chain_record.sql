-- =============================================================================
-- V16 - M2 奥链区块链存证: 上链记录表
--
-- Decisions:
--   1. Separate table from existing tr_trace_chain (which is harvest-batch level)
--      because each trace_code (BATCH/BOX/FRUIT) needs its own chain record
--      with retry state and tx hash.
--   2. data_snapshot stored as JSON for forensic purposes — verify endpoint
--      hashes the current DB row and compares with the stored snapshot hash.
--   3. chain_status enum kept as INT (not VARCHAR) to match existing project
--      convention (see tr_trace_chain.chain_status, V5).
--   4. retry_count + error_msg let admins drive the manual retry workflow.
-- =============================================================================

CREATE TABLE IF NOT EXISTS `tr_chain_record` (
    `id`                 BIGINT       NOT NULL COMMENT '雪花ID',
    `trace_code`         VARCHAR(64)  NOT NULL COMMENT '关联溯源码（BATCH/BOX/FRUIT 任一粒度）',
    `business_type`      VARCHAR(32)  NOT NULL COMMENT 'BATCH / BOX / FRUIT / cultivation / harvest / trade / warehouse / logistics',
    `business_id`        BIGINT                COMMENT '业务主键（如 trace_code.id 或 trace_batch.id）',
    `data_snapshot`      JSON         NOT NULL COMMENT '上链时的数据快照（用于事后比对）',
    `data_hash`          VARCHAR(64)  NOT NULL COMMENT 'SHA-256 hex of canonical JSON',
    `chain_tx_hash`      VARCHAR(128)          COMMENT '奥链返回的交易哈希',
    `chain_block_height` BIGINT                COMMENT '链上区块高度',
    `chain_status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0待上链 1成功 2失败 3重试中',
    `error_msg`          VARCHAR(512)          COMMENT '失败原因',
    `retry_count`        INT          NOT NULL DEFAULT 0 COMMENT '重试次数',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`          VARCHAR(64),
    `deleted`            TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_trace_code` (`trace_code`),
    KEY `idx_chain_status` (`chain_status`),
    KEY `idx_business` (`business_type`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M2 奥链上链记录';
