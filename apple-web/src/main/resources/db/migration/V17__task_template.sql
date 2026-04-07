-- =============================================================================
-- V17 - M5 AI 作业计划: 物候期模板表
--
-- Stores per-variety, per-month canonical operations (rule-engine v1).
-- Seeded by DataInitializer at startup with 3 varieties × 12 months × 6 ops
-- = 216 baseline templates.
-- =============================================================================

CREATE TABLE IF NOT EXISTS `pt_task_template` (
    `id`                  BIGINT       NOT NULL,
    `variety`             VARCHAR(32)  NOT NULL COMMENT '苹果品种：红富士/嘎啦/国光',
    `month`               TINYINT      NOT NULL COMMENT '1-12',
    `operation_type`      VARCHAR(16)  NOT NULL COMMENT 'FERTILIZE/PRUNE/THIN/PESTICIDE/IRRIGATE/HARVEST',
    `task_name`           VARCHAR(128) NOT NULL,
    `suggested_day_start` TINYINT      NOT NULL DEFAULT 1  COMMENT '建议月内起始日',
    `suggested_day_end`   TINYINT      NOT NULL DEFAULT 28 COMMENT '建议月内结束日',
    `material_suggestion` VARCHAR(255)          COMMENT '推荐物资',
    `priority`            TINYINT      NOT NULL DEFAULT 3 COMMENT '1-5',
    `description`         TEXT,
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`           VARCHAR(64),
    `deleted`             TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_variety_month_op` (`variety`, `month`, `operation_type`),
    KEY `idx_variety` (`variety`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M5 物候期作业模板';
