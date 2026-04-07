-- =============================================================================
-- V13 - M6 Maturity Standard + Maturity Record (harvest time recommendation)
-- Source: design-patches-12-missing.md M6
-- Adds:
--   pt_maturity_standard - per-variety baseline thresholds (3 seed varieties)
--   pt_maturity_record   - measured samples + computed maturity score
-- =============================================================================

CREATE TABLE IF NOT EXISTS `pt_maturity_standard` (
    `id`                       BIGINT       NOT NULL COMMENT 'Snowflake ID',
    `variety`                  VARCHAR(32)  NOT NULL COMMENT 'Apple variety code/name (red_fuji/gala/golden_delicious)',
    `brix_min`                 DECIMAL(4,2) NOT NULL COMMENT 'Min sugar Brix degree',
    `brix_max`                 DECIMAL(4,2) NOT NULL COMMENT 'Max sugar Brix degree',
    `firmness_min`             DECIMAL(4,2) NOT NULL COMMENT 'Min firmness kg/cm^2',
    `firmness_max`             DECIMAL(4,2) NOT NULL COMMENT 'Max firmness kg/cm^2',
    `color_target`             VARCHAR(32)  NOT NULL COMMENT 'Target color RGB (e.g. C8281E)',
    `accumulate_temp_target`   INT          NOT NULL COMMENT 'Accumulated temperature target',
    `daily_temp_increment`     DECIMAL(5,2)          DEFAULT 18.00 COMMENT 'Average daily accumulated temp increment used for ETA prediction',
    `description`              VARCHAR(255)          COMMENT 'Variety notes',
    `create_time`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`                VARCHAR(64),
    `deleted`                  TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ms_variety` (`variety`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Maturity standard per variety';

CREATE TABLE IF NOT EXISTS `pt_maturity_record` (
    `id`               BIGINT       NOT NULL COMMENT 'Snowflake ID',
    `orchard_id`       BIGINT       NOT NULL COMMENT 'FK farm_orchard.id',
    `variety`          VARCHAR(32)  NOT NULL COMMENT 'Variety at sample time (snapshot)',
    `sample_date`      DATE         NOT NULL COMMENT 'Sample collection date',
    `brix`             DECIMAL(4,2) NOT NULL COMMENT 'Measured sugar Brix degree',
    `firmness`         DECIMAL(4,2) NOT NULL COMMENT 'Measured firmness kg/cm^2',
    `color_rgb`        VARCHAR(16)  NOT NULL COMMENT 'Measured color RGB (e.g. C8281E)',
    `accumulate_temp`  INT          NOT NULL COMMENT 'Accumulated temperature at sample',
    `maturity_score`   DECIMAL(5,2) NOT NULL COMMENT '0-100 weighted score',
    `recommendation`   VARCHAR(16)  NOT NULL COMMENT 'UNRIPE/OPTIMAL/OVERRIPE',
    `operator`         VARCHAR(64)           COMMENT 'Sample operator name',
    `remark`           VARCHAR(255)          COMMENT 'Notes',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`        VARCHAR(64),
    `deleted`          TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_mr_orchard_id` (`orchard_id`),
    KEY `idx_mr_variety` (`variety`),
    KEY `idx_mr_sample_date` (`sample_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Maturity sample record';

-- =============================================================================
-- Seed: 3 variety baseline standards (red_fuji / gala / golden_delicious)
-- Targets sourced from design doc M6 + standard horticulture references.
-- =============================================================================
INSERT INTO `pt_maturity_standard`
    (`id`, `variety`, `brix_min`, `brix_max`, `firmness_min`, `firmness_max`,
     `color_target`, `accumulate_temp_target`, `daily_temp_increment`,
     `description`, `create_time`, `update_time`, `create_by`, `deleted`)
VALUES
(9001, 'red_fuji',         13.50, 16.00, 6.50, 8.50, 'C8281E', 3200, 18.50,
 '红富士：晚熟代表，糖度高、硬度高、深红色，10月中下旬采收最佳',
 NOW(), NOW(), 'system', 0),
(9002, 'gala',              11.00, 13.50, 5.80, 7.80, 'D94A38', 2400, 17.00,
 '嘎拉：早熟，糖度中等、橙红色条纹，8月下旬到9月上旬采收',
 NOW(), NOW(), 'system', 0),
(9003, 'golden_delicious', 12.00, 14.50, 5.50, 7.50, 'E8D547', 2800, 18.00,
 '黄元帅（金冠）：中熟，金黄色、糖度高、口感面甜，9月中下旬采收',
 NOW(), NOW(), 'system', 0);
