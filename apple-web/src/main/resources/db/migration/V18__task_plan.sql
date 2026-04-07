-- =============================================================================
-- V18 - M5 AI 作业计划: 计划表
--
-- One row per generated task instance per orchard per date.
-- Linked back to task_template for traceability of "why this task exists".
-- =============================================================================

CREATE TABLE IF NOT EXISTS `pt_task_plan` (
    `id`                BIGINT       NOT NULL,
    `orchard_id`        BIGINT       NOT NULL COMMENT 'pt_orchard.id',
    `template_id`       BIGINT                COMMENT 'pt_task_template.id (NULL for manually-added plans)',
    `operation_type`    VARCHAR(16)  NOT NULL,
    `task_name`         VARCHAR(128) NOT NULL,
    `plan_date`         DATE         NOT NULL,
    `priority`          TINYINT      NOT NULL DEFAULT 3,
    `material_suggestion` VARCHAR(255),
    `status`            VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/IN_PROGRESS/DONE/SKIPPED',
    `actual_operation_id` BIGINT              COMMENT '关联 cultivation_operation.id',
    `generated_by`      VARCHAR(16)  NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO/MANUAL',
    `remark`            VARCHAR(512),
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`         VARCHAR(64),
    `deleted`           TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_orchard_date` (`orchard_id`, `plan_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M5 作业计划';
