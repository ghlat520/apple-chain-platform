-- =============================================================================
-- V7 - Growth Record table + Purchase Need column additions + Seed Data
-- Fixes: pt_growth_record table was missing from Flyway migrations
--        td_purchase_need was missing entity-required columns
-- =============================================================================

-- ===== pt_growth_record =====
CREATE TABLE IF NOT EXISTS `pt_growth_record` (
    `id`           BIGINT       NOT NULL COMMENT 'Snowflake ID',
    `orchard_id`   BIGINT       NOT NULL COMMENT 'FK farm_orchard.id',
    `record_type`  VARCHAR(32)  NOT NULL COMMENT 'FERTILIZE/SPRAY/IRRIGATE/PRUNE/PEST_CONTROL',
    `operate_date` DATE         NOT NULL COMMENT 'Operation date',
    `operator`     VARCHAR(64)           COMMENT 'Operator name',
    `materials`    TEXT                  COMMENT 'Materials JSON array',
    `weather`      VARCHAR(64)           COMMENT 'Weather conditions',
    `notes`        TEXT                  COMMENT 'Notes',
    `recorded_by`  BIGINT                COMMENT 'Recorder user ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(64),
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_gr_orchard_id` (`orchard_id`),
    KEY `idx_gr_record_type` (`record_type`),
    KEY `idx_gr_operate_date` (`operate_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Growth record table';

-- ===== td_purchase_need: add missing columns =====
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `need_no`       VARCHAR(32)   AFTER `id`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `buyer_id`      BIGINT        AFTER `need_no`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `price_max`     DECIMAL(10,2) AFTER `quantity`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `require_date`  DATE          AFTER `price_max`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `quality`       VARCHAR(8)    AFTER `require_date`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `delivery_addr` VARCHAR(256)  AFTER `quality`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `description`   VARCHAR(512)  AFTER `delivery_addr`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `status`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `create_time`;
ALTER TABLE `td_purchase_need` ADD COLUMN IF NOT EXISTS `create_by`     VARCHAR(64) AFTER `update_time`;

-- =============================================================================
-- Seed Data - pt_growth_record (8 records, all 5 types covered)
-- =============================================================================
INSERT INTO `pt_growth_record`
    (`id`, `orchard_id`, `record_type`, `operate_date`, `operator`, `materials`, `weather`, `notes`, `recorded_by`, `create_time`, `update_time`, `create_by`, `deleted`)
VALUES
(3001, 20001, 'FERTILIZE',    '2025-03-15', '张大农', '[{"name":"有机复合肥","amount":"200","unit":"kg"},{"name":"硫酸钾","amount":"50","unit":"kg"}]', '晴', '春季基肥，均匀撒施', 1001, NOW(), NOW(), 'system', 0),
(3002, 20001, 'SPRAY',        '2025-04-10', '张大农', '[{"name":"多菌灵","amount":"0.5","unit":"kg"},{"name":"水","amount":"200","unit":"L"}]',         '阴', '防治腐烂病，全株喷施', 1001, NOW(), NOW(), 'system', 0),
(3003, 20002, 'IRRIGATE',     '2025-05-20', '李富贵', '[{"name":"滴灌水","amount":"5000","unit":"L"}]',                                                 '晴', '果实膨大期补水', 1001, NOW(), NOW(), 'system', 0),
(3004, 20003, 'PRUNE',        '2025-02-28', '张大农', '[]',                                                                                             '晴', '冬季修剪，去除枯枝病枝', 1001, NOW(), NOW(), 'system', 0),
(3005, 20001, 'PEST_CONTROL', '2025-06-15', '张大农', '[{"name":"吡虫啉","amount":"100","unit":"mL"},{"name":"水","amount":"100","unit":"L"}]',          '多云', '苹果蚜虫防治', 1001, NOW(), NOW(), 'system', 0),
(3006, 20002, 'FERTILIZE',    '2025-07-01', '李富贵', '[{"name":"叶面肥","amount":"2","unit":"kg"},{"name":"水","amount":"100","unit":"L"}]',            '晴', '果实发育期叶面追肥', 1001, NOW(), NOW(), 'system', 0),
(3007, 20004, 'SPRAY',        '2025-08-10', '赵铁柱', '[{"name":"波尔多液","amount":"5","unit":"kg"},{"name":"水","amount":"250","unit":"L"}]',          '阴', '预防轮纹病', 1001, NOW(), NOW(), 'system', 0),
(3008, 20003, 'IRRIGATE',     '2025-09-05', '张大农', '[{"name":"滴灌水","amount":"8000","unit":"L"}]',                                                 '晴', '采前补水，提高果实品质', 1001, NOW(), NOW(), 'system', 0);

-- =============================================================================
-- Seed Data - td_purchase_need (5 records, all 4 statuses covered)
-- =============================================================================
INSERT INTO `td_purchase_need`
    (`id`, `need_no`, `buyer_id`, `variety`, `quantity`, `price_max`, `require_date`, `quality`, `delivery_addr`, `description`, `status`, `create_time`, `update_time`, `create_by`, `deleted`)
VALUES
(80001, 'NED202501010001', 1001, '红富士', 5000.00,  8.50, '2025-11-15', 'A', '陕西省西安市未央区凤城八路', '需要洛川红富士，果径85mm以上，着色面积80%以上', 'PUBLISHED', NOW(), NOW(), 'system', 0),
(80002, 'NED202501010002', 1001, '嘎拉',   3000.00,  6.00, '2025-10-20', 'B', '陕西省西安市雁塔区丈八东路', '嘎拉苹果，果径75mm以上即可',                   'DRAFT',     NOW(), NOW(), 'system', 0),
(80003, 'NED202501010003', 1002, '黄元帅', 8000.00, 7.00, '2025-11-30', 'A', '甘肃省庆阳市西峰区南大街',   '黄元帅出口级，要求无伤果',                     'MATCHED',   NOW(), NOW(), 'system', 0),
(80004, 'NED202501010004', 1003, '秦冠',   2000.00,  5.00, '2025-12-10', 'C', '陕西省咸阳市渭城区东风路',   '秦冠加工用果，等级不限',                       'CLOSED',    NOW(), NOW(), 'system', 0),
(80005, 'NED202501010005', 1002, '红富士', 10000.00, 9.00, '2026-01-15', 'A', '北京市朝阳区望京SOHO',       '春节礼盒用红富士，要求精品级',                 'PUBLISHED', NOW(), NOW(), 'system', 0);
