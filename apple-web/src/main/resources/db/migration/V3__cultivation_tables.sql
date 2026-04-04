-- =============================================================================
-- V3 - Cultivation Batch & Operation Tables + Seed Data
-- Tables: cultivation_batch, cultivation_operation
-- Seed: >=5 rows per table, all status values covered
-- =============================================================================

-- ===== cultivation_batch =====

CREATE TABLE IF NOT EXISTS `cultivation_batch` (
    `id`             BIGINT          NOT NULL COMMENT '雪花ID',
    `batch_code`     VARCHAR(32)     NOT NULL COMMENT '批次编码 CB+yyyyMMdd+seq',
    `orchard_id`     BIGINT          NOT NULL COMMENT 'FK farm_orchard.id',
    `orchard_name`   VARCHAR(128)             COMMENT '果园名称(冗余)',
    `apple_variety`  VARCHAR(64)              COMMENT '苹果品种',
    `plant_year`     INT                      COMMENT '种植年份',
    `expected_yield` DECIMAL(12,2)            COMMENT '预计产量(kg)',
    `actual_yield`   DECIMAL(12,2)            COMMENT '实际产量(kg)',
    `harvest_date`   DATE                     COMMENT '采收日期',
    `status`         VARCHAR(32)     NOT NULL DEFAULT 'PLANTING'
                                     COMMENT 'PLANTING/GROWING/READY_FOR_HARVEST/HARVESTED/CLOSED',
    `remark`         VARCHAR(512)             COMMENT '备注',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cb_batch_code` (`batch_code`),
    KEY `idx_cb_orchard_id` (`orchard_id`),
    KEY `idx_cb_status` (`status`),
    KEY `idx_cb_harvest_date` (`harvest_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='种植批次表';

-- ===== cultivation_operation =====

CREATE TABLE IF NOT EXISTS `cultivation_operation` (
    `id`             BIGINT       NOT NULL COMMENT '雪花ID',
    `batch_id`       BIGINT       NOT NULL COMMENT 'FK cultivation_batch.id',
    `batch_code`     VARCHAR(32)           COMMENT '批次编码(冗余)',
    `operation_type` VARCHAR(32)  NOT NULL COMMENT 'FERTILIZE/PESTICIDE/PRUNE/HARVEST/IRRIGATE/OTHER',
    `operation_date` DATE                  COMMENT '操作日期',
    `operator`       VARCHAR(64)           COMMENT '操作人',
    `materials`      TEXT                  COMMENT '投入品JSON',
    `remark`         VARCHAR(512)          COMMENT '备注',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_co_batch_id` (`batch_id`),
    KEY `idx_co_operation_type` (`operation_type`),
    KEY `idx_co_operation_date` (`operation_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农事操作记录表';

-- =============================================================================
-- Seed Data - cultivation_batch (6 rows, all 5 statuses + CLOSED)
-- =============================================================================

INSERT IGNORE INTO `cultivation_batch`
    (`id`, `batch_code`, `orchard_id`, `orchard_name`, `apple_variety`, `plant_year`,
     `expected_yield`, `actual_yield`, `harvest_date`, `status`, `remark`,
     `create_time`, `update_time`, `create_by`, `deleted`)
VALUES
(60001, 'CB202501010001', 20001, '张家湾苹果园',   '红富士', 2020, 50000.00, 45000.00, '2025-10-05', 'HARVESTED',         '首批红富士采收完成',       NOW(), NOW(), 'system', 0),
(60002, 'CB202501010002', 20002, '李家沟有机果园', '嘎拉',   2021, 30000.00, 28000.00, '2025-10-08', 'HARVESTED',         '嘎拉提前上市，品质良好',   NOW(), NOW(), 'system', 0),
(60003, 'CB202501010003', 20003, '金苹果示范园',   '黄元帅', 2019, 65000.00, NULL,     '2025-10-20', 'READY_FOR_HARVEST', '黄元帅即将采收',           NOW(), NOW(), 'system', 0),
(60004, 'CB202501010004', 20005, '绿野苹果基地',   '秦冠',   2018, 40000.00, NULL,     '2025-09-30', 'GROWING',           '秦冠生长中，套袋完毕',     NOW(), NOW(), 'system', 0),
(60005, 'CB202501010005', 20001, '张家湾苹果园',   '红富士', 2025, 55000.00, NULL,     '2026-10-10', 'PLANTING',          '新一季红富士，刚完成定植', NOW(), NOW(), 'system', 0),
(60006, 'CB202501010006', 20004, '红星家庭农场',   '红富士', 2022, 20000.00, 18500.00, '2025-09-15', 'CLOSED',            '已销售完毕，批次关闭',     NOW(), NOW(), 'system', 0);

-- =============================================================================
-- Seed Data - cultivation_operation (10 rows, all 6 types covered)
-- =============================================================================

INSERT IGNORE INTO `cultivation_operation`
    (`id`, `batch_id`, `batch_code`, `operation_type`, `operation_date`, `operator`,
     `materials`, `remark`, `create_time`, `update_time`, `create_by`, `deleted`)
VALUES
(70001, 60001, 'CB202501010001', 'FERTILIZE',  '2025-03-15', '张大农',
        '[{"name":"有机复合肥","amount":"200","unit":"kg"}]',         '春季基肥',           NOW(), NOW(), 'system', 0),
(70002, 60001, 'CB202501010001', 'PESTICIDE',  '2025-04-10', '张大农',
        '[{"name":"多菌灵","amount":"0.5","unit":"kg"}]',             '腐烂病防治',         NOW(), NOW(), 'system', 0),
(70003, 60001, 'CB202501010001', 'PRUNE',      '2025-02-20', '张大农',
        '[]',                                                          '冬季修剪，去病枝',   NOW(), NOW(), 'system', 0),
(70004, 60001, 'CB202501010001', 'HARVEST',    '2025-10-05', '张大农',
        '[]',                                                          '人工采收完成',       NOW(), NOW(), 'system', 0),
(70005, 60002, 'CB202501010002', 'FERTILIZE',  '2025-07-01', '李富贵',
        '[{"name":"叶面肥","amount":"2","unit":"kg"}]',               '果实膨大期追肥',     NOW(), NOW(), 'system', 0),
(70006, 60002, 'CB202501010002', 'IRRIGATE',   '2025-05-20', '李富贵',
        '[{"name":"滴灌水","amount":"5000","unit":"L"}]',             '果实膨大期补水',     NOW(), NOW(), 'system', 0),
(70007, 60002, 'CB202501010002', 'HARVEST',    '2025-10-08', '李富贵',
        '[]',                                                          '嘎拉采收完成',       NOW(), NOW(), 'system', 0),
(70008, 60003, 'CB202501010003', 'FERTILIZE',  '2025-04-05', '张大农',
        '[{"name":"磷酸二铵","amount":"150","unit":"kg"}]',           '花前追肥',           NOW(), NOW(), 'system', 0),
(70009, 60004, 'CB202501010004', 'PESTICIDE',  '2025-08-10', '赵铁柱',
        '[{"name":"波尔多液","amount":"5","unit":"kg"}]',             '轮纹病防治',         NOW(), NOW(), 'system', 0),
(70010, 60004, 'CB202501010004', 'OTHER',      '2025-09-01', '赵铁柱',
        '[]',                                                          '摘袋，着色管理',     NOW(), NOW(), 'system', 0);
