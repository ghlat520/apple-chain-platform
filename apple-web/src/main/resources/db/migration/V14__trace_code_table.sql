-- =============================================================================
-- V14 - M12 Three-level traceability code (一果一码)
-- New table: trace_code (BATCH / BOX / FRUIT granularity)
-- BATCH codes reuse trace_batch.batch_code via string key; BOX & FRUIT are
-- generated with CRC-16/CCITT-FALSE checksum and linked by parent_code.
-- =============================================================================

CREATE TABLE IF NOT EXISTS `trace_code` (
    `id`           BIGINT       NOT NULL COMMENT '雪花ID',
    `code`         VARCHAR(64)  NOT NULL COMMENT '三级溯源码（BATCH/BOX/FRUIT）',
    `granularity`  VARCHAR(8)   NOT NULL COMMENT '粒度：BATCH / BOX / FRUIT',
    `parent_code`  VARCHAR(64)           COMMENT '父级编码：BOX→batchCode, FRUIT→boxCode',
    `batch_id`     BIGINT       NOT NULL COMMENT '所属 trace_batch.id',
    `crc16`        VARCHAR(4)            COMMENT 'CRC-16/CCITT-FALSE 校验码（4位hex大写）',
    `qr_url`       VARCHAR(512)          COMMENT '二维码 URL（公共扫码入口）',
    `status`       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / VOID / CONSUMED',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(64),
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_code` (`parent_code`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_granularity` (`granularity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='三级溯源码（BATCH/BOX/FRUIT）';
