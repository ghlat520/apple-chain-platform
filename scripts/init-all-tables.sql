-- Apple Chain Platform - Complete Migration Script
-- Generated: 2026-04-13 00:39
-- Execute against: aiin_dev_db01

-- ============================================
-- V1__init_schema.sql
-- ============================================
-- =============================================================================
-- V1 - Apple Chain Platform Initial Schema
-- Covers: farm_farmer, farm_orchard, trace_batch, trace_record, trade_order
-- Compatible with MySQL 8.0 and H2 (MySQL mode)
-- =============================================================================

-- ===== FARM MODULE =====

CREATE TABLE IF NOT EXISTS `farm_farmer` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `farmer_code`     VARCHAR(32)     NOT NULL COMMENT '农户编码 FC+yyyyMMdd+seq',
    `name`            VARCHAR(64)     NOT NULL COMMENT '姓名',
    `phone`           VARCHAR(20)     NOT NULL COMMENT '手机号',
    `id_card`         VARCHAR(20)              COMMENT '身份证号',
    `address`         VARCHAR(256)             COMMENT '地址',
    `registered_area` DECIMAL(10,2)            COMMENT '注册面积(亩)',
    `status`          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_farmer_code` (`farmer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农户信息表';

CREATE TABLE IF NOT EXISTS `farm_orchard` (
    `id`            BIGINT          NOT NULL COMMENT '雪花ID',
    `orchard_code`  VARCHAR(32)     NOT NULL COMMENT '果园编码 OC+yyyyMMdd+seq',
    `orchard_name`  VARCHAR(128)    NOT NULL COMMENT '果园名称',
    `farmer_id`     BIGINT          NOT NULL COMMENT 'FK farm_farmer.id',
    `location`      VARCHAR(512)             COMMENT '地址',
    `area`          DECIMAL(10,2)            COMMENT '面积(亩)',
    `variety`       VARCHAR(64)              COMMENT '苹果品种',
    `planting_year` INT                      COMMENT '种植年份',
    `status`        VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(64),
    `deleted`       TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orchard_code` (`orchard_code`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_orchard_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='果园信息表(MVP)';

-- ===== TRACE MODULE =====

CREATE TABLE IF NOT EXISTS `trace_batch` (
    `id`               BIGINT          NOT NULL COMMENT '雪花ID',
    `batch_code`       VARCHAR(32)     NOT NULL COMMENT '批次编码 TB+yyyyMMdd+seq',
    `orchard_id`       BIGINT          NOT NULL COMMENT 'FK farm_orchard.id',
    `orchard_name`     VARCHAR(128)             COMMENT '果园名称(冗余)',
    `harvest_date`     DATE            NOT NULL COMMENT '采收日期',
    `variety`          VARCHAR(64)              COMMENT '品种',
    `grade`            VARCHAR(4)               COMMENT 'A/B/C',
    `weight`           DECIMAL(12,2)            COMMENT '重量(kg)',
    `status`           VARCHAR(16)     NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/PROCESSING/COMPLETED/SHIPPED',
    `blockchain_hash`  VARCHAR(128)             COMMENT 'SHA-256 integrity hash',
    `create_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`        VARCHAR(64),
    `deleted`          TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_code` (`batch_code`),
    KEY `idx_batch_orchard_id` (`orchard_id`),
    KEY `idx_batch_status` (`status`),
    KEY `idx_harvest_date` (`harvest_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溯源批次表';

CREATE TABLE IF NOT EXISTS `trace_record` (
    `id`             BIGINT          NOT NULL COMMENT '雪花ID',
    `batch_id`       BIGINT          NOT NULL COMMENT 'FK trace_batch.id',
    `batch_code`     VARCHAR(32)              COMMENT '批次编码(冗余)',
    `stage`          VARCHAR(16)     NOT NULL COMMENT 'HARVEST/STORAGE/TRANSPORT/SALE',
    `operator`       VARCHAR(64)              COMMENT '操作人',
    `operator_phone` VARCHAR(20)              COMMENT '操作人手机',
    `location`       VARCHAR(256)             COMMENT '操作地点',
    `temperature`    DOUBLE                   COMMENT '温度(℃)',
    `humidity`       DOUBLE                   COMMENT '湿度(%)',
    `remark`         VARCHAR(512)             COMMENT '备注',
    `record_time`    DATETIME                 COMMENT '记录时间',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_batch_id` (`batch_id`),
    KEY `idx_stage` (`stage`),
    KEY `idx_record_time` (`record_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溯源记录表';

-- ===== TRADE MODULE =====

CREATE TABLE IF NOT EXISTS `trade_order` (
    `id`             BIGINT          NOT NULL COMMENT '雪花ID',
    `order_no`       VARCHAR(32)     NOT NULL COMMENT '订单号 ORD+yyyyMMdd+seq',
    `buyer_id`       BIGINT                   COMMENT '采购方ID',
    `buyer_name`     VARCHAR(64)              COMMENT '采购方名称',
    `buyer_phone`    VARCHAR(20)              COMMENT '采购方手机',
    `orchard_id`     BIGINT                   COMMENT 'FK farm_orchard.id',
    `orchard_name`   VARCHAR(128)             COMMENT '果园名称(冗余)',
    `batch_code`     VARCHAR(32)              COMMENT '溯源批次编码',
    `variety`        VARCHAR(64)              COMMENT '品种',
    `grade`          VARCHAR(4)               COMMENT 'A/B/C',
    `quantity`       DECIMAL(12,2)            COMMENT '数量(kg)',
    `unit_price`     DECIMAL(10,2)            COMMENT '单价(元/kg)',
    `total_amount`   DECIMAL(14,2)            COMMENT '总金额(元)',
    `status`         VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/SHIPPED/COMPLETED/CANCELLED',
    `payment_status` VARCHAR(16)     NOT NULL DEFAULT 'UNPAID'  COMMENT 'UNPAID/PAID',
    `remark`         VARCHAR(512),
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_order_orchard_id` (`orchard_id`),
    KEY `idx_order_status` (`status`),
    KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收购交易订单表(MVP)';


-- ============================================
-- V2__seed_data.sql
-- ============================================
-- =============================================================================
-- V2 - Seed Data for MVP Tables
-- At least 5 rows per table, all status values covered
-- =============================================================================

-- ===== farm_farmer =====
INSERT INTO `farm_farmer` (`id`, `farmer_code`, `name`, `phone`, `id_card`, `address`, `registered_area`, `status`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(10001, 'FC202501010001', '张大农', '13811111101', '610426198501015001', '陕西省延安市洛川县旧县镇张家湾村', 120.50, 'ACTIVE',   NOW(), NOW(), 'system', 0),
(10002, 'FC202501010002', '李富贵', '13811111102', '610426197803025002', '陕西省延安市洛川县交口河镇李家沟', 85.20,  'ACTIVE',   NOW(), NOW(), 'system', 0),
(10003, 'FC202501010003', '王花花', '13811111103', '610426199012035003', '陕西省延安市宜川县丹州镇红旗村',   65.80,  'ACTIVE',   NOW(), NOW(), 'system', 0),
(10004, 'FC202501010004', '赵铁柱', '13811111104', '610426197605045004', '陕西省延安市黄陵县店头镇绿野村',   150.30, 'ACTIVE',   NOW(), NOW(), 'system', 0),
(10005, 'FC202501010005', '孙老根', '13811111105', '610426196808055005', '陕西省延安市志丹县顺宁镇向阳村',   45.00,  'INACTIVE', NOW(), NOW(), 'system', 0);

-- ===== farm_orchard =====
INSERT INTO `farm_orchard` (`id`, `orchard_code`, `orchard_name`, `farmer_id`, `location`, `area`, `variety`, `planting_year`, `status`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(20001, 'OC202501010001', '张家湾苹果园',    10001, '陕西省延安市洛川县旧县镇张家湾村',  120.50, '红富士', 2020, 'ACTIVE',   NOW(), NOW(), 'system', 0),
(20002, 'OC202501010002', '李家沟有机果园',  10002, '陕西省延安市洛川县交口河镇李家沟',  85.20,  '嘎拉',   2021, 'ACTIVE',   NOW(), NOW(), 'system', 0),
(20003, 'OC202501010003', '金苹果示范园',    10001, '陕西省延安市洛川县凤栖镇金家塬',   200.00, '黄元帅', 2019, 'ACTIVE',   NOW(), NOW(), 'system', 0),
(20004, 'OC202501010004', '红星家庭农场',    10003, '陕西省延安市宜川县丹州镇红旗村',   65.80,  '红富士', 2022, 'INACTIVE', NOW(), NOW(), 'system', 0),
(20005, 'OC202501010005', '绿野苹果基地',    10004, '陕西省延安市黄陵县店头镇绿野村',   150.30, '秦冠',   2018, 'ACTIVE',   NOW(), NOW(), 'system', 0);

-- ===== trace_batch =====
INSERT INTO `trace_batch` (`id`, `batch_code`, `orchard_id`, `orchard_name`, `harvest_date`, `variety`, `grade`, `weight`, `status`, `blockchain_hash`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(30001, 'TB202510010001', 20001, '张家湾苹果园',   '2025-10-05', '红富士', 'A', 45000.00, 'SHIPPED',    'a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef12345678', NOW(), NOW(), 'system', 0),
(30002, 'TB202510010002', 20002, '李家沟有机果园', '2025-10-08', '嘎拉',   'B', 28000.00, 'COMPLETED',  'b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef12345679', NOW(), NOW(), 'system', 0),
(30003, 'TB202510010003', 20003, '金苹果示范园',   '2025-10-12', '黄元帅', 'A', 62000.00, 'PROCESSING', 'c3d4e5f6789012345678901234567890abcdef1234567890abcdef1234567890', NOW(), NOW(), 'system', 0),
(30004, 'TB202510010004', 20005, '绿野苹果基地',   '2025-09-28', '秦冠',   'C', 38000.00, 'CREATED',    'd4e5f6789012345678901234567890abcdef1234567890abcdef12345678901a', NOW(), NOW(), 'system', 0),
(30005, 'TB202510010005', 20001, '张家湾苹果园',   '2025-10-20', '红富士', 'B', 51000.00, 'CREATED',    'e5f6789012345678901234567890abcdef1234567890abcdef12345678901ab2', NOW(), NOW(), 'system', 0);

-- ===== trace_record =====
INSERT INTO `trace_record` (`id`, `batch_id`, `batch_code`, `stage`, `operator`, `operator_phone`, `location`, `temperature`, `humidity`, `remark`, `record_time`, `create_time`) VALUES
(40001, 30001, 'TB202510010001', 'HARVEST',   '张大农', '13811111101', '陕西省延安市洛川县旧县镇',   18.5, 65.0, '采收完成，果实成熟度良好',               '2025-10-05 08:00:00', NOW()),
(40002, 30001, 'TB202510010001', 'STORAGE',   '刘仓管', '13844444401', '洛川县冷链仓储中心',         2.0,  95.0, '入库检验合格，冷链保存',                 '2025-10-06 14:00:00', NOW()),
(40003, 30001, 'TB202510010001', 'TRANSPORT', '孙物流', '13855555501', '陕西省西安市至北京市高速', 4.0,  90.0, '冷链运输，全程温控监控',                 '2025-10-07 10:00:00', NOW()),
(40004, 30001, 'TB202510010001', 'SALE',      '赵采购', '13833333301', '北京市新发地农产品批发市场', NULL, NULL, '到货验收合格，分销入场',                 '2025-10-09 09:00:00', NOW()),
(40005, 30002, 'TB202510010002', 'HARVEST',   '李富贵', '13811111102', '陕西省延安市洛川县交口河镇', 16.0, 70.0, '嘎拉采收，提前上市',                     '2025-10-08 07:30:00', NOW()),
(40006, 30002, 'TB202510010002', 'STORAGE',   '刘仓管', '13844444401', '洛川县冷链仓储中心',         1.5,  92.0, '入库，分级包装',                         '2025-10-09 15:00:00', NOW()),
(40007, 30003, 'TB202510010003', 'HARVEST',   '张大农', '13811111101', '陕西省延安市洛川县凤栖镇',   20.0, 60.0, '黄元帅大批次采收',                       '2025-10-12 06:30:00', NOW()),
(40008, 30003, 'TB202510010003', 'STORAGE',   '刘仓管', '13844444401', '洛川县冷链仓储中心',         2.5,  93.0, '黄元帅入库，二次分级',                   '2025-10-13 10:00:00', NOW()),
(40009, 30004, 'TB202510010004', 'HARVEST',   '赵铁柱', '13811111104', '陕西省延安市黄陵县店头镇',   19.0, 55.0, '秦冠采收，C级品',                        '2025-09-28 09:00:00', NOW()),
(40010, 30005, 'TB202510010005', 'HARVEST',   '张大农', '13811111101', '陕西省延安市洛川县旧县镇',   17.5, 68.0, '张家湾第二批次红富士采收完成',           '2025-10-20 07:00:00', NOW());

-- ===== trade_order =====
INSERT INTO `trade_order` (`id`, `order_no`, `buyer_id`, `buyer_name`, `buyer_phone`, `orchard_id`, `orchard_name`, `batch_code`, `variety`, `grade`, `quantity`, `unit_price`, `total_amount`, `status`, `payment_status`, `remark`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(50001, 'TO202510010001', 1005, '赵采购(北京新发地)', '13833333301', 20001, '张家湾苹果园',   'TB202510010001', '红富士', 'A', 15000.00, 4.60, 69000.00,  'COMPLETED', 'PAID',   '首批合同，顺利交付', NOW(), NOW(), 'system', 0),
(50002, 'TO202510010002', 1006, '陈超市(永辉超市)',   '13833333302', 20002, '李家沟有机果园', 'TB202510010002', '嘎拉',   'B', 8000.00,  3.90, 31200.00,  'SHIPPED',   'PAID',   '超市渠道，已发货',   NOW(), NOW(), 'system', 0),
(50003, 'TO202510010003', 1005, '赵采购(北京新发地)', '13833333301', 20003, '金苹果示范园',   'TB202510010003', '黄元帅', 'A', 20000.00, 3.50, 70000.00,  'CONFIRMED', 'UNPAID', '大批次，等待付款',   NOW(), NOW(), 'system', 0),
(50004, 'TO202510010004', 1006, '陈超市(永辉超市)',   '13833333302', 20001, '张家湾苹果园',   'TB202510010005', '红富士', 'B', 5000.00,  4.20, 21000.00,  'PENDING',   'UNPAID', '等待确认',           NOW(), NOW(), 'system', 0),
(50005, 'TO202510010005', 1005, '赵采购(北京新发地)', '13833333301', 20003, '金苹果示范园',   'TB202510010003', '黄元帅', 'A', 10000.00, 3.40, 34000.00,  'CANCELLED', 'UNPAID', '买家取消',           NOW(), NOW(), 'system', 0);


-- ============================================
-- V3__cultivation_tables.sql
-- ============================================
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


-- ============================================
-- V4__user_and_dashboard_tables.sql
-- ============================================
-- =============================================================================
-- V4 - User table + Dashboard-required tables (missing from V1-V3)
-- =============================================================================

-- ===== uc_user (AuthController login) =====
CREATE TABLE IF NOT EXISTS `uc_user` (
    `id`          BIGINT          NOT NULL,
    `username`    VARCHAR(64)     NOT NULL,
    `password`    VARCHAR(255)    NOT NULL,
    `real_name`   VARCHAR(64),
    `phone`       VARCHAR(20),
    `email`       VARCHAR(128),
    `role_code`   VARCHAR(32),
    `status`      INT             NOT NULL DEFAULT 1,
    `org_name`    VARCHAR(128),
    `org_type`    VARCHAR(32),
    `avatar`      VARCHAR(512),
    `remark`      VARCHAR(512),
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(64),
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
);

-- Seed users (BCrypt of "admin123")
INSERT INTO `uc_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role_code`, `status`, `org_name`, `org_type`, `deleted`) VALUES
(1001, 'admin',      '$2a$10$HjCRtXg8.TXCiGkcgnVrHOPCr21D/h/OubAU39k.mKIzKcAr19H7y', '系统管理员', '13800000001', 'admin@apple-chain.com',    'ADMIN',    1, '苹果产业链平台', '运营方',   0),
(1002, 'farmer01',   '$2a$10$HjCRtXg8.TXCiGkcgnVrHOPCr21D/h/OubAU39k.mKIzKcAr19H7y', '张大农',    '13811111101', 'farmer01@example.com',    'FARMER',   1, '洛川张家果园',  '个体农户', 0),
(1003, 'buyer01',    '$2a$10$HjCRtXg8.TXCiGkcgnVrHOPCr21D/h/OubAU39k.mKIzKcAr19H7y', '赵采购',    '13833333301', 'buyer01@example.com',     'BUYER',    1, '北京新发地',    '采购商',   0);

-- ===== td_supply_info (SupplyInfoController, DashboardMapper) =====
CREATE TABLE IF NOT EXISTS `td_supply_info` (
    `id`             BIGINT          NOT NULL,
    `supply_no`      VARCHAR(32),
    `farmer_id`      BIGINT,
    `orchard_id`     BIGINT,
    `variety`        VARCHAR(64),
    `quantity`       DECIMAL(12,2),
    `price_expected` DECIMAL(10,2),
    `harvest_date`   DATE,
    `valid_until`    DATE,
    `quality`        VARCHAR(4),
    `location`       VARCHAR(256),
    `description`    VARCHAR(512),
    `status`         VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- ===== td_trade_order (TradeOrderController original, DashboardMapper) =====
CREATE TABLE IF NOT EXISTS `td_trade_order` (
    `id`             BIGINT          NOT NULL,
    `order_no`       VARCHAR(32),
    `supply_id`      BIGINT,
    `need_id`        BIGINT,
    `farmer_id`      BIGINT,
    `buyer_id`       BIGINT,
    `variety`        VARCHAR(64),
    `quantity`       DECIMAL(12,2),
    `unit_price`     DECIMAL(10,2),
    `total_amount`   DECIMAL(14,2),
    `trade_date`     DATE,
    `delivery_date`  DATE,
    `payment_status` VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    `order_status`   VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    `contract_file`  VARCHAR(512),
    `remark`         VARCHAR(512),
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- ===== td_purchase_need (DashboardMapper) =====
CREATE TABLE IF NOT EXISTS `td_purchase_need` (
    `id`          BIGINT          NOT NULL,
    `variety`     VARCHAR(64),
    `quantity`    DECIMAL(12,2),
    `status`      VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- ===== pt_orchard (DashboardMapper - different from farm_orchard) =====
CREATE TABLE IF NOT EXISTS `pt_orchard` (
    `id`           BIGINT          NOT NULL,
    `orchard_name` VARCHAR(128),
    `orchard_no`   VARCHAR(32),
    `variety`      VARCHAR(64),
    `area`         DECIMAL(10,2),
    `deleted`      TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- ===== pt_harvest_batch (DashboardMapper) =====
CREATE TABLE IF NOT EXISTS `pt_harvest_batch` (
    `id`           BIGINT          NOT NULL,
    `orchard_id`   BIGINT,
    `total_weight` DECIMAL(12,2),
    `harvest_date` DATE,
    `status`       VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    `deleted`      TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- ===== tr_trace_chain (TraceController + DashboardMapper) =====
CREATE TABLE IF NOT EXISTS `tr_trace_chain` (
    `id`             BIGINT          NOT NULL,
    `trace_code`     VARCHAR(32),
    `product_type`   VARCHAR(64),
    `batch_no`       VARCHAR(32),
    `orchard_id`     BIGINT,
    `farmer_id`      BIGINT,
    `current_status` VARCHAR(32),
    `data_hash`      VARCHAR(128),
    `chain_status`   INT             DEFAULT 0,
    `remark`         VARCHAR(512),
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- ===== tr_trace_node (TraceController) =====
CREATE TABLE IF NOT EXISTS `tr_trace_node` (
    `id`             BIGINT          NOT NULL,
    `trace_code`     VARCHAR(32),
    `node_type`      VARCHAR(16),
    `node_time`      DATETIME,
    `operator_id`    BIGINT,
    `operator_name`  VARCHAR(64),
    `summary`        VARCHAR(256),
    `detail`         TEXT,
    `location`       VARCHAR(256),
    `data_hash`      VARCHAR(128),
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- Seed trace chains (linked to existing trace_batch and farm_orchard)
INSERT INTO `tr_trace_chain` (`id`, `trace_code`, `product_type`, `batch_no`, `orchard_id`, `farmer_id`, `current_status`, `data_hash`, `chain_status`, `remark`, `deleted`) VALUES
(3001, 'TC20260301001', '红富士苹果', 'TB20260301001', 2001, 1001, 'SOLD',       'a1b2c3d4e5f6', 1, '已完成全链路溯源', 0),
(3002, 'TC20260301002', '嘎啦苹果',   'TB20260301002', 2002, 1001, 'IN_TRANSIT',  'f6e5d4c3b2a1', 1, '运输中',           0),
(3003, 'TC20260302001', '红富士苹果', 'TB20260302001', 2001, 1002, 'HARVESTED',   'b2c3d4e5f6a1', 0, '已采收待入库',     0),
(3004, 'TC20260303001', '秦冠苹果',   'TB20260303001', 2003, 1002, 'IN_STORAGE',  'c3d4e5f6a1b2', 1, '冷库储藏中',       0),
(3005, 'TC20260304001', '黄元帅苹果', 'TB20260304001', 2004, 1003, 'PLANTED',     'd4e5f6a1b2c3', 0, '种植阶段',         0);

-- Seed trace nodes (timeline for TC20260301001 — full lifecycle)
INSERT INTO `tr_trace_node` (`id`, `trace_code`, `node_type`, `node_time`, `operator_id`, `operator_name`, `summary`, `detail`, `location`, `data_hash`, `deleted`) VALUES
(4001, 'TC20260301001', 'PLANT',     '2025-03-15 08:00:00', 1001, '张大农', '红富士种植',       '{"variety":"红富士","area":"5亩","soilPH":6.5}',              '陕西洛川红富士基地', 'hash001', 0),
(4002, 'TC20260301001', 'GROW',      '2025-06-20 10:00:00', 1001, '张大农', '施肥除虫',         '{"fertilizer":"有机肥","pesticide":"生物农药","weather":"晴"}', '陕西洛川红富士基地', 'hash002', 0),
(4003, 'TC20260301001', 'HARVEST',   '2025-10-01 06:00:00', 1001, '张大农', '采收入库',         '{"weight":"2500kg","grade":"A","temperature":12}',            '陕西洛川红富士基地', 'hash003', 0),
(4004, 'TC20260301001', 'STORAGE',   '2025-10-02 14:00:00', 1001, '张大农', '冷库储藏',         '{"warehouse":"洛川冷库A区","temperature":-1,"humidity":90}',   '洛川冷库A区',        'hash004', 0),
(4005, 'TC20260301001', 'LOGISTICS', '2025-10-10 08:00:00', 1003, '赵采购', '冷链运输至北京',   '{"vehicle":"陕A12345","driver":"李师傅","temperature":2}',     '西安→北京',          'hash005', 0),
(4006, 'TC20260301001', 'TRADE',     '2025-10-12 16:00:00', 1003, '赵采购', '交易完成',         '{"buyer":"北京新发地","price":"8.5元/kg","total":"21250元"}',   '北京新发地市场',     'hash006', 0),
-- Nodes for TC20260301002
(4007, 'TC20260301002', 'PLANT',     '2025-04-01 08:00:00', 1001, '张大农', '嘎啦苹果种植',     '{"variety":"嘎啦","area":"3亩"}',                              '陕西洛川嘎啦园',     'hash007', 0),
(4008, 'TC20260301002', 'HARVEST',   '2025-09-15 07:00:00', 1001, '张大农', '嘎啦采收',         '{"weight":"1800kg","grade":"A"}',                              '陕西洛川嘎啦园',     'hash008', 0),
(4009, 'TC20260301002', 'LOGISTICS', '2025-09-20 09:00:00', 1003, '赵采购', '运输中',           '{"vehicle":"陕B67890","destination":"上海"}',                  '西安→上海',          'hash009', 0);


-- ============================================
-- V5__trace_linkage.sql
-- ============================================
-- =============================================================================
-- V5 - Full trace linkage across all business modules
-- Adds trace_code to: cultivation_batch, td_supply_info
-- Updates trade_order seed data with batch_code linkage
-- =============================================================================

-- ===== 1. cultivation_batch: add trace_code column =====
ALTER TABLE `cultivation_batch` ADD COLUMN `trace_code` VARCHAR(32) COMMENT '溯源码，关联 tr_trace_chain.trace_code';

-- ===== 2. td_supply_info: add batch_code + trace_code columns =====
ALTER TABLE `td_supply_info` ADD COLUMN `batch_code` VARCHAR(32) COMMENT '种植批次编码';
ALTER TABLE `td_supply_info` ADD COLUMN `trace_code` VARCHAR(32) COMMENT '溯源码';

-- ===== 3. Update cultivation_batch with trace_code linkage =====
-- CB202501010001 (张家湾红富士 HARVESTED) → TC20260301001
UPDATE `cultivation_batch` SET `trace_code` = 'TC20260301001' WHERE `batch_code` = 'CB202501010001';
-- CB202501010002 (李家沟嘎拉 HARVESTED) → TC20260301002
UPDATE `cultivation_batch` SET `trace_code` = 'TC20260301002' WHERE `batch_code` = 'CB202501010002';
-- CB202501010003 (金苹果黄元帅 READY_FOR_HARVEST) → TC20260304001
UPDATE `cultivation_batch` SET `trace_code` = 'TC20260304001' WHERE `batch_code` = 'CB202501010003';
-- CB202501010004 (绿野秦冠 GROWING) → TC20260303001
UPDATE `cultivation_batch` SET `trace_code` = 'TC20260303001' WHERE `batch_code` = 'CB202501010004';
-- CB202501010006 (红星红富士 CLOSED) → TC20260302001
UPDATE `cultivation_batch` SET `trace_code` = 'TC20260302001' WHERE `batch_code` = 'CB202501010006';

-- ===== 4. Insert supply info with trace linkage (replacing empty seed data) =====
INSERT INTO `td_supply_info` (`id`, `supply_no`, `farmer_id`, `orchard_id`, `variety`, `quantity`, `price_expected`,
    `harvest_date`, `quality`, `location`, `description`, `status`, `batch_code`, `trace_code`, `deleted`)
VALUES
(8001, 'SUP20260301001', 1002, 20001, '红富士', 15000.00, 4.60, '2025-10-05', 'A', '陕西省延安市洛川县', '张家湾2025年首批红富士，已完成全链路溯源', 'MATCHED', 'CB202501010001', 'TC20260301001', 0),
(8002, 'SUP20260301002', 1002, 20002, '嘎拉',   8000.00,  3.90, '2025-10-08', 'B', '陕西省延安市洛川县', '李家沟有机嘎拉，运输中',               'MATCHED', 'CB202501010002', 'TC20260301002', 0),
(8003, 'SUP20260302001', 1002, 20003, '黄元帅', 20000.00, 3.50, '2025-10-20', 'A', '陕西省延安市洛川县', '金苹果示范园黄元帅，即将采收',         'PUBLISHED', 'CB202501010003', 'TC20260304001', 0),
(8004, 'SUP20260303001', 1003, 20005, '秦冠',   10000.00, 2.80, '2025-09-28', 'C', '陕西省延安市黄陵县', '绿野基地秦冠，生长中',                 'DRAFT',   'CB202501010004', 'TC20260303001', 0),
(8005, 'SUP20260304001', 1002, 20004, '红富士', 5000.00,  4.20, '2025-09-15', 'B', '陕西省延安市宜川县', '红星家庭农场红富士',                   'CLOSED',  'CB202501010006', 'TC20260302001', 0);

-- ===== 5. Update trade_order with batch_code linking to cultivation_batch =====
-- Order 50001 (红富士 COMPLETED) → batch CB202501010001
UPDATE `trade_order` SET `batch_code` = 'CB202501010001' WHERE `id` = 50001;
-- Order 50002 (嘎拉 SHIPPED) → batch CB202501010002
UPDATE `trade_order` SET `batch_code` = 'CB202501010002' WHERE `id` = 50002;
-- Order 50003 (黄元帅 CONFIRMED) → batch CB202501010003
UPDATE `trade_order` SET `batch_code` = 'CB202501010003' WHERE `id` = 50003;
-- Order 50004 (红富士 PENDING) → batch CB202501010005
UPDATE `trade_order` SET `batch_code` = 'CB202501010005' WHERE `id` = 50004;
-- Order 50005 (黄元帅 CANCELLED) → batch CB202501010003
UPDATE `trade_order` SET `batch_code` = 'CB202501010003' WHERE `id` = 50005;


-- ============================================
-- V6__input_warehouse_tables.sql
-- ============================================
-- =============================================================================
-- V6 - Agricultural Input (农资) & Warehouse (仓储) Module Tables + Seed Data
-- Tables: agri_product, agri_supplier, agri_purchase, agri_inventory, agri_usage,
--         wh_warehouse, wh_record, wh_receipt
-- Seed: all status values covered per table for data closure testing
-- Compatible with MySQL 8.0 and H2 (MySQL mode)
-- =============================================================================

-- ===== 1. agri_product (农资产品) =====

CREATE TABLE IF NOT EXISTS `agri_product` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `product_code`    VARCHAR(32)              COMMENT '产品编码',
    `name`            VARCHAR(128)    NOT NULL COMMENT '产品名称',
    `type`            VARCHAR(32)     NOT NULL DEFAULT 'FERTILIZER'
                                     COMMENT 'FERTILIZER/PESTICIDE/SEED/TOOL',
    `manufacturer`    VARCHAR(128)             COMMENT '生产厂家',
    `spec`            VARCHAR(64)              COMMENT '规格',
    `batch_no`        VARCHAR(64)              COMMENT '生产批号',
    `production_date` DATE                     COMMENT '生产日期',
    `expiry_date`     DATE                     COMMENT '保质期至',
    `registration`    VARCHAR(64)              COMMENT '登记证号/农药登记号',
    `status`          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE'
                                     COMMENT 'ACTIVE/DISCONTINUED',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_ap_type` (`type`),
    KEY `idx_ap_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农资产品表';

-- ===== 2. agri_supplier (农资供应商) =====

CREATE TABLE IF NOT EXISTS `agri_supplier` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `supplier_code`   VARCHAR(32)              COMMENT '供应商编码',
    `name`            VARCHAR(128)    NOT NULL COMMENT '供应商名称',
    `contact_person`  VARCHAR(64)              COMMENT '联系人',
    `phone`           VARCHAR(20)              COMMENT '联系电话',
    `address`         VARCHAR(256)             COMMENT '地址',
    `license`         VARCHAR(64)              COMMENT '营业执照号',
    `qualification`   VARCHAR(128)             COMMENT '资质证书',
    `credit_score`    INT             DEFAULT 80 COMMENT '信用评分(0-100)',
    `status`          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE'
                                     COMMENT 'ACTIVE/SUSPENDED/BLACKLISTED',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_as_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农资供应商表';

-- ===== 3. agri_purchase (农资采购记录) =====

CREATE TABLE IF NOT EXISTS `agri_purchase` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `purchase_no`     VARCHAR(32)              COMMENT '采购单号',
    `product_id`      BIGINT                   COMMENT 'FK agri_product.id',
    `product_name`    VARCHAR(128)             COMMENT '产品名称(冗余)',
    `supplier_id`     BIGINT                   COMMENT 'FK agri_supplier.id',
    `supplier_name`   VARCHAR(128)             COMMENT '供应商名称(冗余)',
    `quantity`        DECIMAL(12,2)            COMMENT '采购数量',
    `unit`            VARCHAR(16)              COMMENT '单位',
    `unit_price`      DECIMAL(10,2)            COMMENT '单价',
    `total_amount`    DECIMAL(12,2)            COMMENT '总金额',
    `purchase_date`   DATE                     COMMENT '采购日期',
    `farmer_id`       BIGINT                   COMMENT 'FK farm_farmer.id',
    `status`          VARCHAR(16)     NOT NULL DEFAULT 'PENDING'
                                     COMMENT 'PENDING/RECEIVED/CANCELLED',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_apu_farmer_id` (`farmer_id`),
    KEY `idx_apu_product_id` (`product_id`),
    KEY `idx_apu_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农资采购记录表';

-- ===== 4. agri_inventory (农资库存) =====

CREATE TABLE IF NOT EXISTS `agri_inventory` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `product_id`      BIGINT                   COMMENT 'FK agri_product.id',
    `product_name`    VARCHAR(128)             COMMENT '产品名称(冗余)',
    `farmer_id`       BIGINT                   COMMENT 'FK farm_farmer.id',
    `stock_quantity`  DECIMAL(12,2)   DEFAULT 0 COMMENT '库存数量',
    `unit`            VARCHAR(16)              COMMENT '单位',
    `warning_level`   DECIMAL(12,2)            COMMENT '预警阈值',
    `warehouse`       VARCHAR(128)             COMMENT '存放位置',
    `status`          VARCHAR(16)     NOT NULL DEFAULT 'NORMAL'
                                     COMMENT 'NORMAL/LOW/EMPTY',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_ai_farmer_id` (`farmer_id`),
    KEY `idx_ai_product_id` (`product_id`),
    KEY `idx_ai_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农资库存表';

-- ===== 5. agri_usage (农资使用记录) =====

CREATE TABLE IF NOT EXISTS `agri_usage` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `product_id`      BIGINT                   COMMENT 'FK agri_product.id',
    `product_name`    VARCHAR(128)             COMMENT '产品名称(冗余)',
    `batch_id`        BIGINT                   COMMENT 'FK cultivation_batch.id',
    `batch_code`      VARCHAR(32)              COMMENT '种植批次编码(冗余)',
    `operation_id`    BIGINT                   COMMENT 'FK cultivation_operation.id',
    `orchard_id`      BIGINT                   COMMENT 'FK farm_orchard.id',
    `orchard_name`    VARCHAR(128)             COMMENT '果园名称(冗余)',
    `quantity`        DECIMAL(12,2)            COMMENT '使用数量',
    `unit`            VARCHAR(16)              COMMENT '单位',
    `usage_date`      DATE                     COMMENT '使用日期',
    `operator`        VARCHAR(64)              COMMENT '操作人',
    `method`          VARCHAR(32)              COMMENT '施用方式(撒施/喷洒/穴施等)',
    `trace_code`      VARCHAR(32)              COMMENT '溯源码，关联 tr_trace_chain.trace_code',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_au_batch_id` (`batch_id`),
    KEY `idx_au_orchard_id` (`orchard_id`),
    KEY `idx_au_trace_code` (`trace_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农资使用记录表';

-- ===== 6. wh_warehouse (仓库) =====

CREATE TABLE IF NOT EXISTS `wh_warehouse` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `warehouse_code`  VARCHAR(32)              COMMENT '仓库编码',
    `name`            VARCHAR(128)    NOT NULL COMMENT '仓库名称',
    `type`            VARCHAR(32)     NOT NULL DEFAULT 'NORMAL'
                                     COMMENT 'NORMAL/COLD/ATMOSPHERE',
    `location`        VARCHAR(256)             COMMENT '仓库地址',
    `capacity`        DECIMAL(12,2)            COMMENT '总容量(吨)',
    `used_capacity`   DECIMAL(12,2)   DEFAULT 0 COMMENT '已用容量(吨)',
    `temperature`     DECIMAL(5,2)             COMMENT '仓储温度(℃)',
    `humidity`        DECIMAL(5,2)             COMMENT '仓储湿度(%)',
    `manager`         VARCHAR(64)              COMMENT '仓库管理员',
    `phone`           VARCHAR(20)              COMMENT '联系电话',
    `status`          VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE'
                                     COMMENT 'ACTIVE/FULL/MAINTENANCE',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_ww_type` (`type`),
    KEY `idx_ww_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- ===== 7. wh_record (出入库记录) =====

CREATE TABLE IF NOT EXISTS `wh_record` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `record_no`       VARCHAR(32)              COMMENT '出入库单号',
    `warehouse_id`    BIGINT                   COMMENT 'FK wh_warehouse.id',
    `warehouse_name`  VARCHAR(128)             COMMENT '仓库名称(冗余)',
    `record_type`     VARCHAR(16)     NOT NULL COMMENT 'INBOUND/OUTBOUND',
    `batch_code`      VARCHAR(32)              COMMENT '种植批次编码',
    `variety`         VARCHAR(64)              COMMENT '苹果品种',
    `grade`           VARCHAR(4)               COMMENT '等级(A/B/C)',
    `quantity`        DECIMAL(12,2)            COMMENT '数量(kg)',
    `temperature`     DECIMAL(5,2)             COMMENT '入库时温度(℃)',
    `humidity`        DECIMAL(5,2)             COMMENT '入库时湿度(%)',
    `operator`        VARCHAR(64)              COMMENT '操作人',
    `record_date`     DATE                     COMMENT '操作日期',
    `trace_code`      VARCHAR(32)              COMMENT '溯源码',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_wr_warehouse_id` (`warehouse_id`),
    KEY `idx_wr_record_type` (`record_type`),
    KEY `idx_wr_batch_code` (`batch_code`),
    KEY `idx_wr_trace_code` (`trace_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出入库记录表';

-- ===== 8. wh_receipt (仓单) =====

CREATE TABLE IF NOT EXISTS `wh_receipt` (
    `id`              BIGINT          NOT NULL COMMENT '雪花ID',
    `receipt_no`      VARCHAR(32)              COMMENT '仓单编号',
    `warehouse_id`    BIGINT                   COMMENT 'FK wh_warehouse.id',
    `warehouse_name`  VARCHAR(128)             COMMENT '仓库名称(冗余)',
    `farmer_id`       BIGINT                   COMMENT 'FK farm_farmer.id',
    `farmer_name`     VARCHAR(64)              COMMENT '农户姓名(冗余)',
    `batch_code`      VARCHAR(32)              COMMENT '种植批次编码',
    `variety`         VARCHAR(64)              COMMENT '苹果品种',
    `grade`           VARCHAR(4)               COMMENT '等级(A/B/C)',
    `quantity`        DECIMAL(12,2)            COMMENT '数量(kg)',
    `unit_value`      DECIMAL(10,2)            COMMENT '单价(元/kg)',
    `total_value`     DECIMAL(12,2)            COMMENT '总价值(元)',
    `inbound_date`    DATE                     COMMENT '入库日期',
    `valid_until`     DATE                     COMMENT '仓单有效期',
    `trace_code`      VARCHAR(32)              COMMENT '溯源码',
    `status`          VARCHAR(16)     NOT NULL DEFAULT 'VALID'
                                     COMMENT 'VALID/PLEDGED/TRANSFERRED/CANCELLED',
    `remark`          VARCHAR(512)             COMMENT '备注',
    `create_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_wrc_warehouse_id` (`warehouse_id`),
    KEY `idx_wrc_farmer_id` (`farmer_id`),
    KEY `idx_wrc_status` (`status`),
    KEY `idx_wrc_trace_code` (`trace_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓单表';


-- =============================================================================
-- SEED DATA
-- =============================================================================

-- ===== agri_product: 6 records (FERTILIZER/PESTICIDE/SEED/TOOL × ACTIVE/DISCONTINUED) =====

INSERT IGNORE INTO `agri_product`
    (`id`, `product_code`, `name`, `type`, `manufacturer`, `spec`, `batch_no`, `production_date`, `expiry_date`, `registration`, `status`, `remark`, `create_by`, `deleted`)
VALUES
(90001, 'AP20250001', '有机复合肥',           'FERTILIZER', '山东金正大生态工程股份有限公司', '50kg/袋',  'PB20250101', '2025-01-15', '2027-01-15', 'FER-2025-0088',  'ACTIVE',       '氮磷钾15-15-15，适用于果树追肥',         'system', 0),
(90002, 'AP20250002', '多菌灵可湿性粉剂',     'PESTICIDE',  '江苏辉丰生物农业股份有限公司', '500g/袋',  'PB20250201', '2025-02-10', '2027-02-10', 'PD20251234',     'ACTIVE',       '广谱性杀菌剂，防治苹果褐斑病',           'system', 0),
(90003, 'AP20250003', '吡虫啉悬浮剂',         'PESTICIDE',  '拜耳作物科学（中国）有限公司', '200ml/瓶', 'PB20250301', '2025-03-05', '2027-03-05', 'PD20255678',     'ACTIVE',       '内吸性杀虫剂，防治蚜虫',                 'system', 0),
(90004, 'AP20250004', '红富士嫁接苗',         'SEED',       '陕西省果树研究所',             '株',       'PB20250401', '2025-01-20', '2026-01-20', 'SD-2025-0456',   'ACTIVE',       '两年生矮化砧嫁接苗，适合密植',           'system', 0),
(90005, 'AP20250005', '果树修剪剪刀',         'TOOL',       '日本冈田工具株式会社',         '把',       'PB20250501', '2024-11-01', NULL,         NULL,             'ACTIVE',       'SK-5型专业果树修剪剪刀，锰钢材质',       'system', 0),
(90006, 'AP20250006', '磷酸二铵',             'FERTILIZER', '云天化集团有限责任公司',       '50kg/袋',  'PB20240601', '2024-06-01', '2026-06-01', 'FER-2024-0055',  'DISCONTINUED', '已停用，替换为有机复合肥',               'system', 0);

-- ===== agri_supplier: 5 records (ACTIVE/SUSPENDED/BLACKLISTED) =====

INSERT IGNORE INTO `agri_supplier`
    (`id`, `supplier_code`, `name`, `contact_person`, `phone`, `address`, `license`, `qualification`, `credit_score`, `status`, `remark`, `create_by`, `deleted`)
VALUES
(91001, 'SUP-LC-001', '洛川农资直供中心',   '王建国', '13909110001', '陕西省延安市洛川县凤栖镇农资市场A区3号', '91610629MA7XXXXX1', '农药经营许可证/肥料登记证', 92, 'ACTIVE',      '洛川县重点农资供应商，合作5年',       'system', 0),
(91002, 'SUP-YA-001', '延安绿丰农化公司',   '刘芳',   '13909110002', '陕西省延安市宝塔区南关工业园区',         '91610602MA7XXXXX2', '农药经营许可证',             85, 'ACTIVE',      '延安地区农化产品经销商',             'system', 0),
(91003, 'SUP-SX-001', '陕西果苗培育基地',   '陈明',   '13909110003', '陕西省杨凌农业高新技术产业示范区',       '91610403MA7XXXXX3', '林木种子生产经营许可证',     88, 'ACTIVE',      '省级果苗良种繁育基地',               'system', 0),
(91004, 'SUP-HL-001', '黄陵农机服务站',     '张伟',   '13909110004', '陕西省延安市黄陵县店头镇',               '91610632MA7XXXXX4', '农机销售许可证（已过期）',   60, 'SUSPENDED',   '资质过期，暂停合作，待续证后恢复',   'system', 0),
(91005, 'SUP-XX-001', 'XX农资店',           '不详',   '13909110005', '陕西省延安市洛川县交口河镇',             '91610629MA7XXXXX5', NULL,                         20, 'BLACKLISTED', '销售假冒伪劣农药，已永久拉黑',       'system', 0);

-- ===== agri_purchase: 5 records (PENDING/RECEIVED/CANCELLED) =====
-- farmer_id: 10001=张大农, 10002=李富贵, 10003=赵铁柱 (from V2 seed)

INSERT IGNORE INTO `agri_purchase`
    (`id`, `purchase_no`, `product_id`, `product_name`, `supplier_id`, `supplier_name`, `quantity`, `unit`, `unit_price`, `total_amount`, `purchase_date`, `farmer_id`, `status`, `remark`, `create_by`, `deleted`)
VALUES
(92001, 'PUR20250310001', 90001, '有机复合肥',           91001, '洛川农资直供中心', 200.00, 'kg',  1.60,  320.00,  '2025-03-10', 10001, 'RECEIVED',  '春季基肥采购，已验收入库',                     'system', 0),
(92002, 'PUR20250405001', 90002, '多菌灵可湿性粉剂',     91002, '延安绿丰农化公司', 2.00,   'kg',  35.00, 70.00,   '2025-04-05', 10001, 'RECEIVED',  '花期前病害预防用药',                           'system', 0),
(92003, 'PUR20250620001', 90003, '吡虫啉悬浮剂',         91001, '洛川农资直供中心', 1.00,   'L',   88.00, 88.00,   '2025-06-20', 10002, 'RECEIVED',  '夏季蚜虫防治用药',                             'system', 0),
(92004, 'PUR20250801001', 90001, '有机复合肥',           91002, '延安绿丰农化公司', 300.00, 'kg',  1.55,  465.00,  '2025-08-01', 10003, 'PENDING',   '秋季追肥采购，待发货',                         'system', 0),
(92005, 'PUR20250815001', 90005, '果树修剪剪刀',         91005, 'XX农资店',         5.00,   '把',  45.00, 225.00,  '2025-08-15', 10001, 'CANCELLED', '供应商已被拉黑，取消订单',                     'system', 0);

-- ===== agri_inventory: 5 records (NORMAL/LOW/EMPTY) =====

INSERT IGNORE INTO `agri_inventory`
    (`id`, `product_id`, `product_name`, `farmer_id`, `stock_quantity`, `unit`, `warning_level`, `warehouse`, `status`, `remark`, `create_by`, `deleted`)
VALUES
(93001, 90001, '有机复合肥',           10001, 150.00, 'kg',  50.00,  '张家湾果园农资房',   'NORMAL', '春季采购200kg，已使用50kg',       'system', 0),
(93002, 90002, '多菌灵可湿性粉剂',     10001, 0.20,   'kg',  0.50,   '张家湾果园农资房',   'LOW',    '库存不足，建议尽快补货',         'system', 0),
(93003, 90003, '吡虫啉悬浮剂',         10002, 0.00,   'ml',  50.00,  '李家沟果园储藏室',   'EMPTY',  '已用完，需补充采购',             'system', 0),
(93004, 90001, '有机复合肥',           10003, 200.00, 'kg',  80.00,  '赵家坡果园仓库',     'NORMAL', '待发货采购单未到，现有库存充足', 'system', 0),
(93005, 90004, '红富士嫁接苗',         10001, 30.00,  '株',  10.00,  '张家湾果园育苗区',   'NORMAL', '今春定植剩余苗木',               'system', 0);

-- ===== agri_usage: 8 records (linked to cultivation batches & orchards) =====
-- batch_id: 60001=CB202501010001, 60002=CB202501010002, 60003=CB202501010003, 60004=CB202501010004
-- orchard_id: 20001=张家湾红富士, 20002=李家沟嘎拉, 20003=金苹果黄元帅, 20005=绿野秦冠

INSERT IGNORE INTO `agri_usage`
    (`id`, `product_id`, `product_name`, `batch_id`, `batch_code`, `operation_id`, `orchard_id`, `orchard_name`, `quantity`, `unit`, `usage_date`, `operator`, `method`, `trace_code`, `remark`, `create_by`, `deleted`)
VALUES
(94001, 90001, '有机复合肥',           60001, 'CB202501010001', 70001, 20001, '张家湾红富士园', 30.00,  'kg',  '2025-03-15', '张大农', '撒施', 'TC20260301001', '春季基肥，沟施覆土',                   'system', 0),
(94002, 90002, '多菌灵可湿性粉剂',     60001, 'CB202501010001', 70002, 20001, '张家湾红富士园', 0.50,   'kg',  '2025-04-10', '张大农', '喷洒', 'TC20260301001', '花前喷施预防褐斑病，稀释800倍液',     'system', 0),
(94003, 90003, '吡虫啉悬浮剂',         60002, 'CB202501010002', 70003, 20002, '李家沟嘎拉园',   0.20,   'L',   '2025-06-25', '李富贵', '喷洒', 'TC20260301002', '夏季蚜虫防治，稀释2000倍液',         'system', 0),
(94004, 90001, '有机复合肥',           60002, 'CB202501010002', 70004, 20002, '李家沟嘎拉园',   20.00,  'kg',  '2025-07-01', '李富贵', '穴施', 'TC20260301002', '膨果期追肥，树冠投影下穴施',         'system', 0),
(94005, 90001, '有机复合肥',           60003, 'CB202501010003', 70005, 20003, '金苹果示范园',   25.00,  'kg',  '2025-04-05', '王秀英', '撒施', 'TC20260304001', '黄元帅春季施肥',                       'system', 0),
(94006, 90002, '多菌灵可湿性粉剂',     60004, 'CB202501010004', 70006, 20005, '绿野生态果园',   0.80,   'kg',  '2025-08-10', '赵铁柱', '喷洒', 'TC20260303001', '秦冠夏季病害防治，稀释600倍液',       'system', 0),
(94007, 90003, '吡虫啉悬浮剂',         60004, 'CB202501010004', 70007, 20005, '绿野生态果园',   0.30,   'L',   '2025-08-15', '赵铁柱', '喷洒', 'TC20260303001', '秦冠虫害防治，配合粘虫板使用',       'system', 0),
(94008, 90001, '有机复合肥',           60004, 'CB202501010004', 70008, 20005, '绿野生态果园',   20.00,  'kg',  '2025-09-01', '赵铁柱', '穴施', 'TC20260303001', '秋季追肥促花芽分化',                   'system', 0);

-- ===== wh_warehouse: 4 records (NORMAL/COLD/ATMOSPHERE × ACTIVE/FULL/MAINTENANCE) =====

INSERT IGNORE INTO `wh_warehouse`
    (`id`, `warehouse_code`, `name`, `type`, `location`, `capacity`, `used_capacity`, `temperature`, `humidity`, `manager`, `phone`, `status`, `remark`, `create_by`, `deleted`)
VALUES
(95001, 'WH-LC-A', '洛川冷链仓储A库',   'COLD',       '陕西省延安市洛川县凤栖镇工业园区A-1', 500.00,  350.00, -1.00,  90.00, '马冬梅', '13909220001', 'ACTIVE',      '苹果冷藏库，温度-1~0℃，湿度85-95%',           'system', 0),
(95002, 'WH-LC-B', '洛川冷链仓储B库',   'COLD',       '陕西省延安市洛川县凤栖镇工业园区A-2', 300.00,  300.00, -2.00,  92.00, '马冬梅', '13909220001', 'FULL',        '已满库，暂停入库，待出库后恢复',               'system', 0),
(95003, 'WH-YA-A', '延安气调保鲜库',     'ATMOSPHERE', '陕西省延安市宝塔区南关冷链物流园B区',  800.00,  200.00, 0.00,   95.00, '孙大圣', '13909220002', 'ACTIVE',      'CA气调库，O2浓度2-3%，CO2浓度3-5%',            'system', 0),
(95004, 'WH-HL-A', '黄陵普通仓库',       'NORMAL',     '陕西省延安市黄陵县店头镇物流中心',     200.00,  0.00,   15.00,  60.00, '张铁蛋', '13909220003', 'MAINTENANCE', '设备检修中，预计2025年11月恢复使用',           'system', 0);

-- ===== wh_record: 6 records (INBOUND/OUTBOUND) =====

INSERT IGNORE INTO `wh_record`
    (`id`, `record_no`, `warehouse_id`, `warehouse_name`, `record_type`, `batch_code`, `variety`, `grade`, `quantity`, `temperature`, `humidity`, `operator`, `record_date`, `trace_code`, `remark`, `create_by`, `deleted`)
VALUES
(96001, 'WR20251006001', 95001, '洛川冷链仓储A库',   'INBOUND',  'CB202501010001', '红富士', 'A', 15000.00, -1.00, 90.00, '马冬梅', '2025-10-06', 'TC20260301001', '张家湾红富士首批入库，预冷后入冷藏',   'system', 0),
(96002, 'WR20251009001', 95001, '洛川冷链仓储A库',   'INBOUND',  'CB202501010002', '嘎拉',   'B', 8000.00,  -1.00, 88.00, '马冬梅', '2025-10-09', 'TC20260301002', '李家沟嘎拉果入库，品质B级',           'system', 0),
(96003, 'WR20251015001', 95003, '延安气调保鲜库',     'INBOUND',  'CB202501010003', '黄元帅', 'A', 20000.00, 0.00,  95.00, '孙大圣', '2025-10-15', 'TC20260304001', '金苹果示范园黄元帅入气调库长期保鲜',   'system', 0),
(96004, 'WR20251008001', 95001, '洛川冷链仓储A库',   'OUTBOUND', 'CB202501010001', '红富士', 'A', 15000.00, -1.00, 90.00, '马冬梅', '2025-10-08', 'TC20260301001', '发货给赵采购商，冷链运输至西安',       'system', 0),
(96005, 'WR20251001001', 95002, '洛川冷链仓储B库',   'INBOUND',  'CB202501010004', '秦冠',   'C', 10000.00, -2.00, 92.00, '马冬梅', '2025-10-01', 'TC20260303001', '绿野基地秦冠入库，品质C级',           'system', 0),
(96006, 'WR20251012001', 95001, '洛川冷链仓储A库',   'OUTBOUND', 'CB202501010002', '嘎拉',   'B', 5000.00,  -1.00, 89.00, '马冬梅', '2025-10-12', 'TC20260301002', '嘎拉部分出库，发往延安批发市场',       'system', 0);

-- ===== wh_receipt: 4 records (VALID/PLEDGED/TRANSFERRED/CANCELLED) =====

INSERT IGNORE INTO `wh_receipt`
    (`id`, `receipt_no`, `warehouse_id`, `warehouse_name`, `farmer_id`, `farmer_name`, `batch_code`, `variety`, `grade`, `quantity`, `unit_value`, `total_value`, `inbound_date`, `valid_until`, `trace_code`, `status`, `remark`, `create_by`, `deleted`)
VALUES
(97001, 'RC20251006001', 95001, '洛川冷链仓储A库', 10001, '张大农', 'CB202501010001', '红富士', 'A', 15000.00, 4.60,  69000.00, '2025-10-06', '2026-04-06', 'TC20260301001', 'TRANSFERRED', '已转让给赵采购商，仓单所有权变更',           'system', 0),
(97002, 'RC20251009001', 95001, '洛川冷链仓储A库', 10002, '李富贵', 'CB202501010002', '嘎拉',   'B', 8000.00,  3.90,  31200.00, '2025-10-09', '2026-04-09', 'TC20260301002', 'VALID',       '有效仓单，可用于质押贷款或现货交易',         'system', 0),
(97003, 'RC20251015001', 95003, '延安气调保鲜库',   10001, '张大农', 'CB202501010003', '黄元帅', 'A', 20000.00, 3.50,  70000.00, '2025-10-15', '2026-07-15', 'TC20260304001', 'PLEDGED',     '仓单质押贷款中，贷款金额49000元（70%）',     'system', 0),
(97004, 'RC20251001001', 95002, '洛川冷链仓储B库', 10003, '赵铁柱', 'CB202501010004', '秦冠',   'C', 10000.00, 2.80,  28000.00, '2025-10-01', '2026-04-01', 'TC20260303001', 'CANCELLED',   '品质不达标退库，仓单已作废',                 'system', 0);


-- ============================================
-- V7__growth_record_and_purchase_need.sql
-- ============================================
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
ALTER TABLE `td_purchase_need` ADD COLUMN `need_no`       VARCHAR(32)   AFTER `id`;
ALTER TABLE `td_purchase_need` ADD COLUMN `buyer_id`      BIGINT        AFTER `need_no`;
ALTER TABLE `td_purchase_need` ADD COLUMN `price_max`     DECIMAL(10,2) AFTER `quantity`;
ALTER TABLE `td_purchase_need` ADD COLUMN `require_date`  DATE          AFTER `price_max`;
ALTER TABLE `td_purchase_need` ADD COLUMN `quality`       VARCHAR(8)    AFTER `require_date`;
ALTER TABLE `td_purchase_need` ADD COLUMN `delivery_addr` VARCHAR(256)  AFTER `quality`;
ALTER TABLE `td_purchase_need` ADD COLUMN `description`   VARCHAR(512)  AFTER `delivery_addr`;
ALTER TABLE `td_purchase_need` ADD COLUMN `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `status`;
ALTER TABLE `td_purchase_need` ADD COLUMN `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP AFTER `create_time`;
ALTER TABLE `td_purchase_need` ADD COLUMN `create_by`     VARCHAR(64) AFTER `update_time`;

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


-- ============================================
-- V8__coldchain_tables.sql
-- ============================================
-- =============================================
-- V8: Cold-chain logistics module tables
-- =============================================

-- 1. Vehicles
CREATE TABLE IF NOT EXISTS cc_vehicle (
    id              BIGINT       PRIMARY KEY,
    vehicle_code    VARCHAR(32)  NOT NULL COMMENT '车辆编码 VH+日期+序号',
    plate_number    VARCHAR(20)  NOT NULL COMMENT '车牌号',
    vehicle_type    VARCHAR(20)  NOT NULL COMMENT 'REFRIGERATED/INSULATED/NORMAL 冷藏/保温/普通',
    brand           VARCHAR(50)  NULL     COMMENT '品牌型号',
    capacity        DECIMAL(10,2) NULL    COMMENT '载重(吨)',
    volume          DECIMAL(10,2) NULL    COMMENT '容积(m3)',
    temperature_min DECIMAL(5,2)  NULL    COMMENT '最低控温(C)',
    temperature_max DECIMAL(5,2)  NULL    COMMENT '最高控温(C)',
    driver_name     VARCHAR(50)  NULL     COMMENT '驾驶员',
    driver_phone    VARCHAR(20)  NULL     COMMENT '驾驶员电话',
    status          VARCHAR(20)  DEFAULT 'IDLE' COMMENT 'IDLE/IN_TRANSIT/MAINTENANCE/RETIRED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 2. Transport tasks
CREATE TABLE IF NOT EXISTS cc_transport_task (
    id              BIGINT       PRIMARY KEY,
    task_code       VARCHAR(32)  NOT NULL COMMENT '任务编码 TT+日期+序号',
    vehicle_id      BIGINT       NULL     COMMENT '关联车辆ID',
    order_id        BIGINT       NULL     COMMENT '关联订单ID',
    origin          VARCHAR(200) NOT NULL COMMENT '出发地',
    destination     VARCHAR(200) NOT NULL COMMENT '目的地',
    cargo_desc      VARCHAR(200) NULL     COMMENT '货物描述',
    cargo_weight    DECIMAL(10,2) NULL    COMMENT '货物重量(kg)',
    required_temp   DECIMAL(5,2)  NULL    COMMENT '要求温度(C)',
    plan_depart     DATETIME     NULL     COMMENT '计划发车时间',
    actual_depart   DATETIME     NULL     COMMENT '实际发车时间',
    plan_arrive     DATETIME     NULL     COMMENT '计划到达时间',
    actual_arrive   DATETIME     NULL     COMMENT '实际到达时间',
    distance        DECIMAL(10,2) NULL    COMMENT '运输距离(km)',
    cost            DECIMAL(12,2) NULL    COMMENT '运输费用',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/IN_TRANSIT/DELIVERED/CANCELLED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 3. Temperature records (IoT simulation)
CREATE TABLE IF NOT EXISTS cc_temperature_record (
    id              BIGINT       PRIMARY KEY,
    task_id         BIGINT       NOT NULL COMMENT '关联运输任务ID',
    vehicle_id      BIGINT       NULL     COMMENT '关联车辆ID',
    temperature     DECIMAL(5,2) NOT NULL COMMENT '记录温度(C)',
    humidity        DECIMAL(5,2) NULL     COMMENT '记录湿度(%)',
    location        VARCHAR(200) NULL     COMMENT '记录位置',
    record_time     DATETIME     NOT NULL COMMENT '记录时间',
    is_alarm        INT          DEFAULT 0 COMMENT '是否超温报警 0否1是',
    alarm_msg       VARCHAR(200) NULL     COMMENT '报警信息',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 4. Delivery / sign-off
CREATE TABLE IF NOT EXISTS cc_delivery (
    id              BIGINT       PRIMARY KEY,
    delivery_code   VARCHAR(32)  NOT NULL COMMENT '配送编码 DL+日期+序号',
    task_id         BIGINT       NOT NULL COMMENT '关联运输任务ID',
    receiver_name   VARCHAR(50)  NOT NULL COMMENT '收货人',
    receiver_phone  VARCHAR(20)  NULL     COMMENT '收货人电话',
    receiver_addr   VARCHAR(200) NULL     COMMENT '收货地址',
    delivery_time   DATETIME     NULL     COMMENT '配送时间',
    sign_time       DATETIME     NULL     COMMENT '签收时间',
    sign_photo      VARCHAR(500) NULL     COMMENT '签收照片URL',
    quality_check   VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/PASSED/REJECTED 质检',
    quality_remark  VARCHAR(500) NULL     COMMENT '质检备注',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/DELIVERING/SIGNED/REJECTED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- =============================================
-- Seed data
-- =============================================

-- Vehicles
INSERT INTO cc_vehicle (id, vehicle_code, plate_number, vehicle_type, brand, capacity, volume, temperature_min, temperature_max, driver_name, driver_phone, status) VALUES
(8001, 'VH202603010001', '陕A12345', 'REFRIGERATED', '福田欧曼', 10.00, 28.00, -5.00, 8.00,  '张师傅', '13900001001', 'IDLE'),
(8002, 'VH202603010002', '陕A23456', 'REFRIGERATED', '解放J6P',  15.00, 40.00, -8.00, 5.00,  '李师傅', '13900001002', 'IN_TRANSIT'),
(8003, 'VH202603010003', '陕A34567', 'INSULATED',    '东风天龙', 12.00, 32.00,  0.00, 15.00, '王师傅', '13900001003', 'IDLE'),
(8004, 'VH202603010004', '陕A45678', 'NORMAL',       '江淮格尔发', 8.00, 22.00, NULL,  NULL,  '赵师傅', '13900001004', 'MAINTENANCE');

-- Transport tasks
INSERT INTO cc_transport_task (id, task_code, vehicle_id, origin, destination, cargo_desc, cargo_weight, required_temp, plan_depart, actual_depart, plan_arrive, actual_arrive, distance, cost, status) VALUES
(8101, 'TT202603010001', 8002, '延安市洛川县果园基地', '西安市雁塔区批发市场', '红富士苹果 500箱', 2500.00, 2.00, '2026-03-01 06:00:00', '2026-03-01 06:30:00', '2026-03-01 12:00:00', NULL, 320.00, 3500.00, 'IN_TRANSIT'),
(8102, 'TT202603020001', 8001, '咸阳市乾县冷库',     '西安市未央区超市配送中心', '嘎啦苹果 300箱', 1500.00, 3.00, '2026-03-02 07:00:00', '2026-03-02 07:15:00', '2026-03-02 10:00:00', '2026-03-02 09:50:00', 80.00, 1200.00, 'DELIVERED'),
(8103, 'TT202603030001', 8003, '宝鸡市凤翔区果园',   '成都市龙泉驿区水果批发市场', '秦冠苹果 800箱', 4000.00, 5.00, '2026-03-05 05:00:00', NULL, '2026-03-05 18:00:00', NULL, 580.00, 6800.00, 'PENDING'),
(8104, 'TT202603040001', 8001, '渭南市白水县冷库',   '北京市新发地批发市场', '红富士苹果 1200箱', 6000.00, 1.00, '2026-03-06 04:00:00', NULL, '2026-03-07 08:00:00', NULL, 1050.00, 12000.00, 'PENDING');

-- Temperature records (for in-transit task 8101)
INSERT INTO cc_temperature_record (id, task_id, vehicle_id, temperature, humidity, location, record_time, is_alarm, alarm_msg) VALUES
(8201, 8101, 8002, 2.10, 85.0, '延安市洛川县', '2026-03-01 06:30:00', 0, NULL),
(8202, 8101, 8002, 2.30, 84.0, '铜川市耀州区', '2026-03-01 08:00:00', 0, NULL),
(8203, 8101, 8002, 5.80, 82.0, '咸阳市三原县', '2026-03-01 09:30:00', 1, '温度偏高，超过要求温度2C，已达5.8C'),
(8204, 8101, 8002, 2.50, 83.0, '西安市临潼区', '2026-03-01 10:30:00', 0, NULL),
(8205, 8102, 8001, 3.20, 86.0, '咸阳市乾县',   '2026-03-02 07:15:00', 0, NULL),
(8206, 8102, 8001, 3.00, 85.0, '西安市未央区', '2026-03-02 09:50:00', 0, NULL);

-- Delivery / sign-off
INSERT INTO cc_delivery (id, delivery_code, task_id, receiver_name, receiver_phone, receiver_addr, delivery_time, sign_time, quality_check, status) VALUES
(8301, 'DL202603020001', 8102, '王经理', '13800002001', '西安市未央区凤城五路超市配送中心', '2026-03-02 09:50:00', '2026-03-02 10:05:00', 'PASSED', 'SIGNED'),
(8302, 'DL202603010001', 8101, '刘主任', '13800002002', '西安市雁塔区电子正街批发市场A区', NULL, NULL, 'PENDING', 'PENDING'),
(8303, 'DL202603050001', 8103, '陈老板', '13800002003', '成都市龙泉驿区国际水果批发城', NULL, NULL, 'PENDING', 'PENDING');


-- ============================================
-- V9__finance_tables.sql
-- ============================================
-- =============================================
-- V9: Supply-chain finance module tables
-- =============================================

-- 1. Credit Rating
CREATE TABLE IF NOT EXISTS sf_credit_rating (
    id              BIGINT       PRIMARY KEY,
    entity_type     VARCHAR(20)  NOT NULL COMMENT 'FARMER/ENTERPRISE/COOPERATIVE 评级主体类型',
    entity_id       BIGINT       NOT NULL COMMENT '主体ID（农户/企业）',
    entity_name     VARCHAR(100) NOT NULL COMMENT '主体名称',
    credit_score    INT          NOT NULL COMMENT '信用评分 0-1000',
    credit_level    VARCHAR(10)  NOT NULL COMMENT 'AAA/AA/A/BBB/BB/B/C',
    assessment_date DATE         NOT NULL COMMENT '评定日期',
    valid_until     DATE         NULL     COMMENT '有效期至',
    trade_score     INT          DEFAULT 0 COMMENT '交易信用分',
    production_score INT         DEFAULT 0 COMMENT '生产信用分',
    financial_score INT          DEFAULT 0 COMMENT '财务信用分',
    assessor        VARCHAR(50)  NULL     COMMENT '评定人',
    remark          VARCHAR(500) NULL,
    status          VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT 'ACTIVE/EXPIRED/REVOKED',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 2. Loan
CREATE TABLE IF NOT EXISTS sf_loan (
    id              BIGINT       PRIMARY KEY,
    loan_code       VARCHAR(32)  NOT NULL COMMENT '贷款编号 LN+日期+序号',
    borrower_name   VARCHAR(100) NOT NULL COMMENT '借款人名称',
    borrower_type   VARCHAR(20)  NOT NULL COMMENT 'FARMER/ENTERPRISE/COOPERATIVE',
    borrower_id     BIGINT       NULL     COMMENT '借款人ID',
    loan_type       VARCHAR(30)  NOT NULL COMMENT 'PLEDGE/RECEIVABLE/CREDIT 仓单质押/应收账款/信用贷',
    amount          DECIMAL(14,2) NOT NULL COMMENT '贷款金额(元)',
    interest_rate   DECIMAL(5,4) NOT NULL COMMENT '年利率',
    term_months     INT          NOT NULL COMMENT '期限(月)',
    apply_date      DATE         NOT NULL COMMENT '申请日期',
    approve_date    DATE         NULL     COMMENT '审批日期',
    disburse_date   DATE         NULL     COMMENT '放款日期',
    due_date        DATE         NULL     COMMENT '到期日期',
    repaid_amount   DECIMAL(14,2) DEFAULT 0 COMMENT '已还金额',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/DISBURSED/REPAID/OVERDUE',
    pledge_id       BIGINT       NULL     COMMENT '关联质押ID',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 3. Warehouse Receipt Pledge (仓单质押)
CREATE TABLE IF NOT EXISTS sf_pledge (
    id              BIGINT       PRIMARY KEY,
    pledge_code     VARCHAR(32)  NOT NULL COMMENT '质押编号 PL+日期+序号',
    receipt_id      BIGINT       NULL     COMMENT '关联仓单ID',
    receipt_code    VARCHAR(32)  NULL     COMMENT '仓单编码',
    pledgor_name    VARCHAR(100) NOT NULL COMMENT '出质人名称',
    pledgee_name    VARCHAR(100) NOT NULL COMMENT '质权人(银行/金融机构)',
    commodity       VARCHAR(100) NULL     COMMENT '质押物品',
    quantity        DECIMAL(12,2) NULL    COMMENT '质押数量',
    unit            VARCHAR(20)  NULL     COMMENT '单位',
    appraised_value DECIMAL(14,2) NULL    COMMENT '评估价值(元)',
    pledge_rate     DECIMAL(5,4) NULL     COMMENT '质押率',
    loan_amount     DECIMAL(14,2) NULL    COMMENT '可贷金额(元)',
    start_date      DATE         NULL     COMMENT '质押起始日',
    end_date        DATE         NULL     COMMENT '质押到期日',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/ACTIVE/RELEASED/DEFAULTED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 4. Risk Record
CREATE TABLE IF NOT EXISTS sf_risk_record (
    id              BIGINT       PRIMARY KEY,
    risk_code       VARCHAR(32)  NOT NULL COMMENT '风控编号 RK+日期+序号',
    related_type    VARCHAR(20)  NOT NULL COMMENT 'LOAN/PLEDGE/CREDIT 关联类型',
    related_id      BIGINT       NULL     COMMENT '关联ID',
    risk_level      VARCHAR(10)  NOT NULL COMMENT 'HIGH/MEDIUM/LOW',
    risk_type       VARCHAR(50)  NOT NULL COMMENT 'OVERDUE/PRICE_DROP/QUALITY/FRAUD 风险类型',
    description     VARCHAR(500) NOT NULL COMMENT '风险描述',
    measure         VARCHAR(500) NULL     COMMENT '处置措施',
    handler         VARCHAR(50)  NULL     COMMENT '处理人',
    handle_time     DATETIME     NULL     COMMENT '处理时间',
    status          VARCHAR(20)  DEFAULT 'OPEN' COMMENT 'OPEN/HANDLING/RESOLVED/CLOSED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- =============================================
-- Seed data
-- =============================================

-- Credit Ratings
INSERT INTO sf_credit_rating (id, entity_type, entity_id, entity_name, credit_score, credit_level, assessment_date, valid_until, trade_score, production_score, financial_score, assessor, status) VALUES
(9001, 'FARMER',     3001, '张三丰', 850, 'AAA', '2026-01-15', '2027-01-15', 280, 290, 280, '信用评估中心', 'ACTIVE'),
(9002, 'FARMER',     3002, '李莫愁', 720, 'AA',  '2026-01-20', '2027-01-20', 240, 250, 230, '信用评估中心', 'ACTIVE'),
(9003, 'ENTERPRISE', 5001, '洛川苹果合作社', 910, 'AAA', '2026-02-01', '2027-02-01', 300, 310, 300, '信用评估中心', 'ACTIVE'),
(9004, 'FARMER',     3003, '王重阳', 580, 'BB',  '2025-06-01', '2026-06-01', 200, 190, 190, '信用评估中心', 'ACTIVE'),
(9005, 'COOPERATIVE',5002, '白水果业联盟', 680, 'A',   '2026-03-01', '2027-03-01', 230, 220, 230, '信用评估中心', 'ACTIVE');

-- Loans
INSERT INTO sf_loan (id, loan_code, borrower_name, borrower_type, loan_type, amount, interest_rate, term_months, apply_date, approve_date, disburse_date, due_date, repaid_amount, status) VALUES
(9101, 'LN202603010001', '张三丰', 'FARMER', 'PLEDGE', 200000.00, 0.0485, 12, '2026-03-01', '2026-03-05', '2026-03-06', '2027-03-06', 0.00, 'DISBURSED'),
(9102, 'LN202603020001', '洛川苹果合作社', 'ENTERPRISE', 'RECEIVABLE', 500000.00, 0.0520, 6, '2026-03-02', '2026-03-08', NULL, NULL, 0.00, 'APPROVED'),
(9103, 'LN202603030001', '李莫愁', 'FARMER', 'CREDIT', 80000.00, 0.0550, 12, '2026-03-03', NULL, NULL, NULL, 0.00, 'PENDING'),
(9104, 'LN202602150001', '王重阳', 'FARMER', 'CREDIT', 50000.00, 0.0600, 6, '2026-02-15', '2026-02-20', '2026-02-21', '2026-08-21', 50000.00, 'REPAID');

-- Pledges
INSERT INTO sf_pledge (id, pledge_code, receipt_code, pledgor_name, pledgee_name, commodity, quantity, unit, appraised_value, pledge_rate, loan_amount, start_date, end_date, status) VALUES
(9201, 'PL202603010001', 'WR202603010001', '张三丰', '陕西农商银行', '红富士苹果(一级)', 50000.00, 'kg', 400000.00, 0.5000, 200000.00, '2026-03-06', '2027-03-06', 'ACTIVE'),
(9202, 'PL202603050001', 'WR202603020001', '洛川苹果合作社', '中国农业银行', '嘎啦苹果(特级)', 80000.00, 'kg', 720000.00, 0.6000, 432000.00, NULL, NULL, 'PENDING'),
(9203, 'PL202602100001', 'WR202602100001', '白水果业联盟', '西安银行', '秦冠苹果(二级)', 30000.00, 'kg', 180000.00, 0.5000, 90000.00, '2026-02-10', '2026-08-10', 'RELEASED');

-- Risk Records
INSERT INTO sf_risk_record (id, risk_code, related_type, related_id, risk_level, risk_type, description, measure, handler, handle_time, status) VALUES
(9301, 'RK202603150001', 'LOAN', 9101, 'LOW', 'PRICE_DROP', '苹果市场价格下跌8%，质押物价值略有缩水', '持续监控，暂不调整质押率', '风控专员A', '2026-03-15 10:00:00', 'RESOLVED'),
(9302, 'RK202603200001', 'PLEDGE', 9201, 'MEDIUM', 'QUALITY', '质押苹果部分出现轻微损耗，需复检', '安排仓库复检，评估损耗比例', NULL, NULL, 'OPEN'),
(9303, 'RK202603250001', 'LOAN', 9103, 'HIGH', 'OVERDUE', '借款人信用评分偏低(BB级)，贷款申请需额外审核', '要求提供追加担保或降低贷款额度', '风控专员B', '2026-03-25 14:00:00', 'HANDLING');


-- ============================================
-- V10__bigdata_mgmt_tables.sql
-- ============================================
-- =============================================
-- V10: Big-data management backbone (10 modules)
-- M2 of the bigdata-platform rollout.
-- All tables use `bd_` prefix and inherit the
-- project convention: snowflake id, soft-delete,
-- create/update audit fields.
-- =============================================

-- ---------------------------------------------
-- 1. Data source registry
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_data_source (
    id              BIGINT        PRIMARY KEY,
    source_code     VARCHAR(64)   NOT NULL COMMENT '数据源编码（唯一）',
    source_name     VARCHAR(128)  NOT NULL COMMENT '数据源名称',
    source_type     VARCHAR(32)   NOT NULL COMMENT 'MYSQL/CLICKHOUSE/KAFKA/HTTP_API/RSS/FILE',
    category        VARCHAR(32)   NOT NULL COMMENT 'INTERNAL/EXTERNAL',
    connect_url     VARCHAR(512)  NULL     COMMENT 'jdbc url / http endpoint',
    username        VARCHAR(128)  NULL,
    password_enc    VARCHAR(512)  NULL     COMMENT 'AES 加密后的密码',
    extra_config    TEXT          NULL     COMMENT 'JSON 扩展配置',
    owner           VARCHAR(64)   NULL,
    status          VARCHAR(16)   DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED/ERROR',
    last_test_time  DATETIME      NULL,
    last_test_result VARCHAR(512) NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_source_code (source_code, deleted)
) COMMENT='大数据-数据源注册表';

-- ---------------------------------------------
-- 2. Collect job + run history
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_collect_job (
    id              BIGINT        PRIMARY KEY,
    job_code        VARCHAR(64)   NOT NULL,
    job_name        VARCHAR(128)  NOT NULL,
    source_id       BIGINT        NOT NULL COMMENT '关联 bd_data_source',
    job_type        VARCHAR(32)   NOT NULL COMMENT 'CDC/SCHEDULED/MANUAL/STREAMING',
    cron_expr       VARCHAR(64)   NULL     COMMENT 'Quartz cron',
    target_table    VARCHAR(128)  NULL     COMMENT '目标表（ClickHouse 库.表）',
    script          TEXT          NULL     COMMENT '执行脚本/SQL/Adapter class',
    enabled         TINYINT       DEFAULT 1,
    retry_times     INT           DEFAULT 3,
    timeout_seconds INT           DEFAULT 300,
    last_run_time   DATETIME      NULL,
    last_run_status VARCHAR(16)   NULL     COMMENT 'SUCCESS/FAILED/RUNNING',
    owner           VARCHAR(64)   NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_job_code (job_code, deleted),
    INDEX idx_source (source_id)
) COMMENT='大数据-采集任务定义';

CREATE TABLE IF NOT EXISTS bd_collect_job_run (
    id              BIGINT        PRIMARY KEY,
    job_id          BIGINT        NOT NULL,
    job_code        VARCHAR(64)   NOT NULL,
    run_status      VARCHAR(16)   NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED/TIMEOUT',
    start_time      DATETIME      NOT NULL,
    end_time        DATETIME      NULL,
    duration_ms     BIGINT        NULL,
    rows_read       BIGINT        DEFAULT 0,
    rows_written    BIGINT        DEFAULT 0,
    error_message   TEXT          NULL,
    trigger_type    VARCHAR(16)   DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/MANUAL/RETRY',
    triggered_by    VARCHAR(64)   NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_job (job_id, start_time),
    INDEX idx_status (run_status)
) COMMENT='大数据-采集任务运行记录';

-- ---------------------------------------------
-- 3. Data asset catalog (table + field metadata)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_data_asset (
    id              BIGINT        PRIMARY KEY,
    asset_code      VARCHAR(128)  NOT NULL COMMENT 'db.schema.table',
    database_name   VARCHAR(64)   NOT NULL,
    schema_name     VARCHAR(64)   NULL,
    table_name      VARCHAR(128)  NOT NULL,
    biz_name        VARCHAR(128)  NULL     COMMENT '业务名称',
    biz_description VARCHAR(1000) NULL     COMMENT '业务说明',
    owner           VARCHAR(64)   NULL     COMMENT '数据 Owner',
    security_level  VARCHAR(16)   DEFAULT 'INTERNAL' COMMENT 'PUBLIC/INTERNAL/SENSITIVE/SECRET',
    source_system   VARCHAR(64)   NULL     COMMENT '来源系统',
    record_count    BIGINT        DEFAULT 0,
    last_update_time DATETIME     NULL,
    tags            VARCHAR(500)  NULL     COMMENT '逗号分隔的标签',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_asset_code (asset_code, deleted)
) COMMENT='大数据-数据资产目录';

CREATE TABLE IF NOT EXISTS bd_data_asset_field (
    id              BIGINT        PRIMARY KEY,
    asset_id        BIGINT        NOT NULL,
    field_name      VARCHAR(128)  NOT NULL,
    field_type      VARCHAR(64)   NULL,
    biz_meaning     VARCHAR(500)  NULL,
    is_sensitive    TINYINT       DEFAULT 0 COMMENT '0=否 1=是',
    sensitive_type  VARCHAR(32)   NULL     COMMENT 'PHONE/ID_CARD/GPS/NAME/EMAIL',
    is_pk           TINYINT       DEFAULT 0,
    nullable        TINYINT       DEFAULT 1,
    default_value   VARCHAR(255)  NULL,
    ordinal         INT           DEFAULT 0,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_asset (asset_id)
) COMMENT='大数据-资产字段元数据';

-- ---------------------------------------------
-- 4. Metric definition + value snapshot
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_metric_definition (
    id              BIGINT        PRIMARY KEY,
    metric_code     VARCHAR(64)   NOT NULL,
    metric_name     VARCHAR(128)  NOT NULL,
    category        VARCHAR(32)   NOT NULL COMMENT 'PLANTING/TRADE/WAREHOUSE/LOGISTICS/FINANCE/TRACE',
    unit            VARCHAR(32)   NULL     COMMENT 'kg/元/%/亩/批',
    calc_formula    TEXT          NULL     COMMENT '口径描述',
    sql_expression  TEXT          NULL     COMMENT 'ClickHouse 查询 SQL',
    data_source_id  BIGINT        NULL,
    refresh_cron    VARCHAR(64)   NULL,
    owner           VARCHAR(64)   NULL,
    is_core         TINYINT       DEFAULT 0 COMMENT '是否核心指标',
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_metric_code (metric_code, deleted)
) COMMENT='大数据-指标定义';

CREATE TABLE IF NOT EXISTS bd_metric_value (
    id              BIGINT        PRIMARY KEY,
    metric_code     VARCHAR(64)   NOT NULL,
    stat_date       DATE          NOT NULL,
    stat_period     VARCHAR(16)   DEFAULT 'DAY' COMMENT 'DAY/WEEK/MONTH/YEAR',
    metric_value    DECIMAL(18,4) NOT NULL,
    dim_json        VARCHAR(1000) NULL     COMMENT '维度 JSON (品种/地区/…)',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_metric_date (metric_code, stat_date)
) COMMENT='大数据-指标取值快照';

-- ---------------------------------------------
-- 5. Data quality rules + check results
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_dq_rule (
    id              BIGINT        PRIMARY KEY,
    rule_code       VARCHAR(64)   NOT NULL,
    rule_name       VARCHAR(128)  NOT NULL,
    asset_id        BIGINT        NULL,
    asset_code      VARCHAR(128)  NULL,
    field_name      VARCHAR(128)  NULL,
    rule_type       VARCHAR(32)   NOT NULL COMMENT 'NOT_NULL/UNIQUE/RANGE/REGEX/ENUM/FRESHNESS',
    rule_expr       TEXT          NULL     COMMENT '规则表达式或 SQL WHERE 片段',
    severity        VARCHAR(16)   DEFAULT 'WARN' COMMENT 'BLOCK/ERROR/WARN/INFO',
    enabled         TINYINT       DEFAULT 1,
    schedule_cron   VARCHAR(64)   NULL,
    owner           VARCHAR(64)   NULL,
    last_run_time   DATETIME      NULL,
    last_score      DECIMAL(5,2)  NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_rule_code (rule_code, deleted)
) COMMENT='大数据-数据质量规则';

CREATE TABLE IF NOT EXISTS bd_dq_check_result (
    id              BIGINT        PRIMARY KEY,
    rule_id         BIGINT        NOT NULL,
    rule_code       VARCHAR(64)   NOT NULL,
    check_time      DATETIME      NOT NULL,
    total_rows      BIGINT        DEFAULT 0,
    bad_rows        BIGINT        DEFAULT 0,
    score           DECIMAL(5,2)  DEFAULT 100.00,
    pass            TINYINT       DEFAULT 1,
    sample_bad_rows TEXT          NULL     COMMENT 'JSON 异常样本（前 20 条）',
    error_message   TEXT          NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_rule_time (rule_id, check_time)
) COMMENT='大数据-数据质量检查结果';

-- ---------------------------------------------
-- 6. Data lineage (upstream -> downstream DAG)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_data_lineage (
    id              BIGINT        PRIMARY KEY,
    upstream_type   VARCHAR(32)   NOT NULL COMMENT 'TABLE/FIELD/JOB/METRIC/DASHBOARD/SYSTEM',
    upstream_id     VARCHAR(256)  NOT NULL COMMENT '上游标识（asset_code/metric_code 等）',
    upstream_name   VARCHAR(256)  NULL,
    downstream_type VARCHAR(32)   NOT NULL,
    downstream_id   VARCHAR(256)  NOT NULL,
    downstream_name VARCHAR(256)  NULL,
    relation_type   VARCHAR(32)   NOT NULL COMMENT 'DERIVES_FROM/TRANSFORMS/AGGREGATES/REFERENCES',
    pipeline_job_id BIGINT        NULL     COMMENT '关联的 collect_job / spark_sql',
    description     VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_upstream (upstream_type, upstream_id),
    INDEX idx_downstream (downstream_type, downstream_id)
) COMMENT='大数据-数据血缘关系';

-- ---------------------------------------------
-- 7. Dashboard config (screen + widgets)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_dashboard_config (
    id              BIGINT        PRIMARY KEY,
    dashboard_code  VARCHAR(64)   NOT NULL,
    dashboard_name  VARCHAR(128)  NOT NULL,
    category        VARCHAR(32)   NOT NULL COMMENT 'GOV/OPS/INDUSTRY/CUSTOM',
    layout_json     TEXT          NULL     COMMENT 'vue-grid-layout JSON',
    theme           VARCHAR(32)   DEFAULT 'dark',
    visibility      VARCHAR(32)   DEFAULT 'PRIVATE' COMMENT 'PUBLIC/PRIVATE/ROLE',
    visible_roles   VARCHAR(500)  NULL     COMMENT '逗号分隔角色码',
    published       TINYINT       DEFAULT 0,
    owner           VARCHAR(64)   NULL,
    refresh_seconds INT           DEFAULT 30,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_dashboard_code (dashboard_code, deleted)
) COMMENT='大数据-大屏配置';

CREATE TABLE IF NOT EXISTS bd_dashboard_widget (
    id              BIGINT        PRIMARY KEY,
    dashboard_id    BIGINT        NOT NULL,
    widget_code     VARCHAR(64)   NOT NULL,
    widget_name     VARCHAR(128)  NOT NULL,
    widget_type     VARCHAR(32)   NOT NULL COMMENT 'KPI/LINE/BAR/PIE/TABLE/MAP/TRACE/TEXT',
    data_source     VARCHAR(32)   NOT NULL COMMENT 'METRIC/SQL/API',
    data_ref        VARCHAR(256)  NOT NULL COMMENT 'metric_code / sql 名 / api 路径',
    position_json   VARCHAR(500)  NULL     COMMENT '{x,y,w,h}',
    option_json     TEXT          NULL     COMMENT 'ECharts option',
    ordinal         INT           DEFAULT 0,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_dashboard (dashboard_id)
) COMMENT='大数据-大屏 Widget';

-- ---------------------------------------------
-- 8. (Audit log - reuse existing audit_log table if present)
--    Only create if missing.
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_audit_log (
    id              BIGINT        PRIMARY KEY,
    user_id         BIGINT        NULL,
    username        VARCHAR(64)   NULL,
    role_code       VARCHAR(32)   NULL,
    module          VARCHAR(64)   NULL,
    action          VARCHAR(64)   NOT NULL,
    target_type     VARCHAR(64)   NULL,
    target_id       VARCHAR(128)  NULL,
    request_ip      VARCHAR(64)   NULL,
    request_uri     VARCHAR(256)  NULL,
    request_method  VARCHAR(16)   NULL,
    request_params  TEXT          NULL,
    response_status INT           NULL,
    duration_ms     BIGINT        NULL,
    error_message   VARCHAR(1000) NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_action (action)
) COMMENT='大数据-管理操作审计日志';

-- ---------------------------------------------
-- 9. Role / Permission matrix
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_role (
    id              BIGINT        PRIMARY KEY,
    role_code       VARCHAR(32)   NOT NULL,
    role_name       VARCHAR(64)   NOT NULL,
    description     VARCHAR(500)  NULL,
    sort_order      INT           DEFAULT 0,
    enabled         TINYINT       DEFAULT 1,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code, deleted)
) COMMENT='大数据-角色字典';

CREATE TABLE IF NOT EXISTS bd_permission (
    id              BIGINT        PRIMARY KEY,
    perm_code       VARCHAR(64)   NOT NULL,
    perm_name       VARCHAR(128)  NOT NULL,
    module          VARCHAR(32)   NOT NULL COMMENT 'BIGDATA/USER/TRADE/…',
    perm_type       VARCHAR(16)   NOT NULL COMMENT 'MENU/BUTTON/API/DATA',
    resource_pattern VARCHAR(256) NULL     COMMENT '菜单路径 / URI Ant pattern',
    parent_code     VARCHAR(64)   NULL,
    sort_order      INT           DEFAULT 0,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_perm_code (perm_code, deleted)
) COMMENT='大数据-权限条目';

CREATE TABLE IF NOT EXISTS bd_role_permission (
    id              BIGINT        PRIMARY KEY,
    role_code       VARCHAR(32)   NOT NULL,
    perm_code       VARCHAR(64)   NOT NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_role (role_code),
    INDEX idx_perm (perm_code)
) COMMENT='大数据-角色权限映射';

-- ---------------------------------------------
-- 10. API gateway (M5 · Open API)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_api_client (
    id              BIGINT        PRIMARY KEY,
    client_name     VARCHAR(128)  NOT NULL,
    app_key         VARCHAR(64)   NOT NULL,
    app_secret_enc  VARCHAR(256)  NOT NULL,
    owner           VARCHAR(64)   NULL,
    rate_limit_qps  INT           DEFAULT 10,
    daily_quota     INT           DEFAULT 10000,
    status          VARCHAR(16)   DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED/REVOKED',
    valid_until     DATE          NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_app_key (app_key, deleted)
) COMMENT='大数据-开放 API 客户端';

CREATE TABLE IF NOT EXISTS bd_api_call_log (
    id              BIGINT        PRIMARY KEY,
    app_key         VARCHAR(64)   NULL,
    client_ip       VARCHAR(64)   NULL,
    api_path        VARCHAR(256)  NOT NULL,
    method          VARCHAR(16)   NOT NULL,
    status_code     INT           NULL,
    duration_ms     BIGINT        NULL,
    request_bytes   BIGINT        NULL,
    response_bytes  BIGINT        NULL,
    error_message   VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_app_time (app_key, create_time),
    INDEX idx_path (api_path)
) COMMENT='大数据-开放 API 调用日志';

-- ---------------------------------------------
-- 11. Analysis report (WEEKLY / MONTHLY / SEASONAL)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_analysis_report (
    id              BIGINT        PRIMARY KEY,
    report_no       VARCHAR(64)   NOT NULL COMMENT 'RPT-W/M/S-period-HHmmss',
    title           VARCHAR(256)  NOT NULL,
    report_type     VARCHAR(32)   NOT NULL COMMENT 'WEEKLY/MONTHLY/SEASONAL',
    period          VARCHAR(32)   NOT NULL COMMENT '2026-W15 / 2026-04 / 2026-Q2',
    status          VARCHAR(16)   DEFAULT 'DRAFT' COMMENT 'DRAFT/GENERATING/PUBLISHED',
    generated_time  DATETIME      NULL,
    planting_section TEXT         NULL COMMENT 'JSON 种植聚合',
    trade_section    TEXT         NULL COMMENT 'JSON 交易聚合',
    warehouse_section TEXT        NULL COMMENT 'JSON 仓储聚合',
    finance_section  TEXT         NULL COMMENT 'JSON 金融聚合',
    summary         VARCHAR(2000) NULL,
    published_by    VARCHAR(64)   NULL,
    published_time  DATETIME      NULL,
    owner           VARCHAR(64)   NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_report_no (report_no, deleted),
    INDEX idx_type_period (report_type, period)
) COMMENT='大数据-分析报告';

-- ---------------------------------------------
-- Seed data: register 8 internal sources + 4 external sources
-- ---------------------------------------------
INSERT INTO bd_data_source (id, source_code, source_name, source_type, category, connect_url, status, remark)
VALUES
(1001, 'SRC_PLANTING',  '种植生产库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '苹果种植生产模块'),
(1002, 'SRC_INPUT',     '农资管理库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '农资投入品模块'),
(1003, 'SRC_TRACE',     '全流程溯源库', 'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '奥链溯源模块'),
(1004, 'SRC_TRADE',     '收购交易库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '收购交易撮合模块'),
(1005, 'SRC_WAREHOUSE', '仓储管理库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '仓储与智能仓单模块'),
(1006, 'SRC_COLDCHAIN', '冷链物流库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '冷链调度追踪模块'),
(1007, 'SRC_FINANCE',   '供应链金融库', 'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '供应链金融模块'),
(1008, 'SRC_USER',      '用户与运营库', 'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '用户/权限/审计'),
(2001, 'EXT_WEATHER',   '和风天气 API','HTTP_API','EXTERNAL','https://devapi.qweather.com/v7', 'ACTIVE', '主产区气象数据'),
(2002, 'EXT_MARKET',    '新发地批发价','HTTP_API','EXTERNAL','http://www.xinfadi.com.cn/api',  'ACTIVE', '农产品批发价日更新'),
(2003, 'EXT_POLICY',    '农业部 RSS', 'RSS',    'EXTERNAL', 'http://www.moa.gov.cn/rss',       'ACTIVE', '政策法规订阅'),
(2004, 'EXT_CONSUMER',  '电商消费趋势','HTTP_API','EXTERNAL','https://open.taobao.com/trend',   'ACTIVE', '电商搜索/消费趋势');

-- Seed: 9 roles
INSERT INTO bd_role (id, role_code, role_name, sort_order, description)
VALUES
(3001, 'ADMIN',     '系统管理员',    1,  '平台最高权限'),
(3002, 'OPS',       '运营管理员',    2,  '业务运营 + 数据看板'),
(3003, 'FARMER',    '种植户',       10, '果园/作业/采收'),
(3004, 'SUPPLIER',  '农资供应商',    11, '农资商品与订单'),
(3005, 'BUYER',     '收购商',       12, '收购需求与下单'),
(3006, 'WAREHOUSE', '仓储服务商',    13, '仓库与仓单'),
(3007, 'LOGISTICS', '物流商',       14, '车辆/运单/温控'),
(3008, 'FINANCE',   '金融机构',     15, '融资审批/风控'),
(3009, 'GOV',       '政府监管',     20, '只读数据/溯源/告警');

-- Seed: 10 core metrics from existing DashboardMapper.xml
INSERT INTO bd_metric_definition (id, metric_code, metric_name, category, unit, calc_formula, is_core, owner)
VALUES
(4001, 'M_TOTAL_ORCHARDS',   '果园总数',        'PLANTING', '个', 'COUNT(pt_orchard WHERE deleted=0)', 1, 'bigdata'),
(4002, 'M_TOTAL_AREA',       '种植总面积',       'PLANTING', '亩', 'SUM(pt_orchard.area)', 1, 'bigdata'),
(4003, 'M_HARVEST_YEAR',     '本年采收总量',     'PLANTING', 'kg', 'SUM(pt_harvest_batch.weight_kg WHERE YEAR=current)', 1, 'bigdata'),
(4004, 'M_TRADE_COUNT',      '成交订单数',       'TRADE',    '笔', 'COUNT(td_trade_order WHERE status=COMPLETED)', 1, 'bigdata'),
(4005, 'M_TRADE_AMOUNT',     '成交总金额',       'TRADE',    '元', 'SUM(td_trade_order.total_amount)', 1, 'bigdata'),
(4006, 'M_HARVEST_TREND',    '近6月采收趋势',    'PLANTING', 'kg', '按月分组求和', 1, 'bigdata'),
(4007, 'M_TRADE_TREND',      '近6月交易趋势',    'TRADE',    '元', '按月分组求和', 1, 'bigdata'),
(4008, 'M_VARIETY_DIST',     '品种分布',        'PLANTING', 'kg', '按品种分组', 1, 'bigdata'),
(4009, 'M_TOP_ORCHARDS',     'TOP5果园',       'PLANTING', 'kg', 'ORDER BY harvest DESC LIMIT 5', 1, 'bigdata'),
(4010, 'M_TRACE_STATUS',     '溯源状态分布',     'TRACE',    '条', '按状态分组', 1, 'bigdata');


-- ============================================
-- V11__m3_seatunnel_bootstrap.sql
-- ============================================
-- =============================================
-- V11: M3 SeaTunnel + DataHub bootstrap
-- D1 决策: SeaTunnel 替代 Canal + 4 Java Adapter
-- D2 决策: DataHub OpenLineage (M4 部署, M3 预留 external_urn)
-- 详见 docs/pipeline/apple-chain-platform/M3-readiness-pack.md
-- =============================================

-- ---------------------------------------------
-- 1. bd_data_lineage 增加 DataHub URN 预留字段
--    M4 DataHub 部署后写入 urn:li:dataset:(...)
-- ---------------------------------------------
ALTER TABLE bd_data_lineage
    ADD COLUMN external_urn VARCHAR(512) NULL COMMENT 'DataHub URN 预留 (M4 启用)' AFTER description,
    ADD INDEX idx_external_urn (external_urn);

-- ---------------------------------------------
-- 2. bd_data_source: 12 条种子 (8 内部 + 4 外部)
--    source_type 值域扩展: SEATUNNEL_CDC / SEATUNNEL_HTTP
-- ---------------------------------------------
INSERT INTO bd_data_source
(id, source_code, source_name, source_type, category, connect_url, username, extra_config, owner, status, remark, create_by)
VALUES
-- 8 内部 (SeaTunnel MySQL-CDC, 同一物理库不同表)
(1101, 'INTERNAL_ORCHARD',       '果园基地业务库',     'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_orchard"]}',          'bigdata', 'ACTIVE', 'F-101 果园档案', 'system'),
(1102, 'INTERNAL_BATCH',         '生产批次库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_batch"]}',            'bigdata', 'ACTIVE', 'F-201 批次管理', 'system'),
(1103, 'INTERNAL_TRACE',         '溯源记录库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_traceability"]}',     'bigdata', 'ACTIVE', 'F-301 全链路溯源', 'system'),
(1104, 'INTERNAL_LOAN',          '产业链贷款库',       'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_loan"]}',     'bigdata', 'ACTIVE', 'F-401 金融贷款', 'system'),
(1105, 'INTERNAL_INSURANCE',     '农业保险库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_insurance"]}','bigdata','ACTIVE', 'F-402 农业保险', 'system'),
(1106, 'INTERNAL_FUTURES',       '期货期权库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_futures"]}',  'bigdata', 'ACTIVE', 'F-403 期货期权', 'system'),
(1107, 'INTERNAL_DEPOSIT',       '冷库订金库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_deposit"]}',  'bigdata', 'ACTIVE', 'F-404 冷库订金', 'system'),
(1108, 'INTERNAL_IOT_DEVICE',    'IoT 设备库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_iot_device"]}',       'bigdata', 'ACTIVE', 'F-501 5G+北斗 IoT', 'system'),
-- 4 外部 (SeaTunnel Http Source)
(1201, 'EXT_WEATHER_HEFENG',     '和风天气',           'SEATUNNEL_HTTP', 'EXTERNAL', 'https://devapi.qweather.com/v7/weather/24h',         NULL,    '{"job":"ext-weather-hefeng","auth_apollo_key":"bigdata.weather.hefeng.apikey"}', 'bigdata', 'ACTIVE', 'F-801 外部气象', 'system'),
(1202, 'EXT_PRICE_XINFADI',      '北京新发地批发价',   'SEATUNNEL_HTTP', 'EXTERNAL', 'http://www.xinfadi.com.cn/getPriceData.html',        NULL,    '{"job":"ext-market-price-xinfadi","auth":"none"}',                              'bigdata', 'ACTIVE', 'F-801 行情参考', 'system'),
(1203, 'EXT_POLICY_MOA_NDRC',    '农业部+发改委 RSS',  'SEATUNNEL_HTTP', 'EXTERNAL', 'http://www.moa.gov.cn/govpublic/rss.xml',            NULL,    '{"job":"ext-policy-moa-ndrc","auth":"none","compliance":"F-805"}',              'bigdata', 'ACTIVE', 'F-805 监管溯源', 'system'),
(1204, 'EXT_CONSUMER_JD',        '京东商智搜索热度',   'SEATUNNEL_HTTP', 'EXTERNAL', 'https://api.jd.com/routerjson',                      NULL,    '{"job":"ext-consumer-trend","auth_apollo_key":"bigdata.jd.appkey"}',            'bigdata', 'ACTIVE', 'F-801 消费趋势', 'system');

-- ---------------------------------------------
-- 3. bd_collect_job: 5 条作业登记
-- ---------------------------------------------
INSERT INTO bd_collect_job
(id, job_code, job_name, source_id, job_type, cron_expr, target_table, script, enabled, retry_times, timeout_seconds, create_by)
VALUES
(2001, 'INTERNAL_APPLE_CHAIN_CDC', '内部业务库 CDC',        1101, 'STREAMING', NULL,           'cdc.apple_chain.*',                           '/opt/seatunnel/jobs/internal/internal-apple-chain-cdc.conf',    1, 3,  0,    'system'),
(2101, 'EXT_WEATHER_HEFENG',       '和风天气 5min 采集',    1201, 'SCHEDULED', '0 */5 * * * ?', 'apple_bigdata_ext.weather_hourly',            '/opt/seatunnel/jobs/external/ext-weather-hefeng.conf',          1, 3,  300,  'system'),
(2102, 'EXT_PRICE_XINFADI',        '新发地批发价日采集',    1202, 'SCHEDULED', '0 30 2 * * ?',  'apple_bigdata_ext.market_price_daily',        '/opt/seatunnel/jobs/external/ext-market-price-xinfadi.conf',    1, 3,  600,  'system'),
(2103, 'EXT_POLICY_MOA_NDRC',      '部委政策 RSS 日采集',   1203, 'SCHEDULED', '0 0 6 * * ?',   'apple_bigdata_ext.policy_article',            '/opt/seatunnel/jobs/external/ext-policy-moa-ndrc.conf',         1, 3,  600,  'system'),
(2104, 'EXT_CONSUMER_TREND',       '电商搜索热度日采集',    1204, 'SCHEDULED', '0 0 3 * * ?',   'apple_bigdata_ext.consumer_trend',            '/opt/seatunnel/jobs/external/ext-consumer-trend.conf',          1, 3,  600,  'system');


-- ============================================
-- V12__rbac_tables.sql
-- ============================================
-- =============================================================================
-- V10 - M1 Six-Role RBAC: roles, permissions, user-role, role-permission, data scopes
-- Spec: docs/pipeline/apple-chain-platform/design-patches-12-missing.md (M1)
--
-- Decisions vs spec:
--   1. Migration number is V10 (next free slot), not V12 — V10 is the next gap.
--   2. role_code keeps the EXISTING uc_user.role_code naming (no ROLE_ prefix)
--      to stay backward-compatible with existing seeded users (admin/farmer01/buyer01)
--      and the JWT roleCode claim already in production.
--   3. Six business roles + ADMIN = 7 rows in sys_role
--      (FARMER, SUPPLIER, BUYER, LOGISTICS, FINANCE, GOV, ADMIN).
--   4. Permission codes follow spec format: <resource>:<action>.
-- =============================================================================

-- ===== sys_role: role definitions =====
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT          NOT NULL,
    `role_code`   VARCHAR(32)     NOT NULL,
    `role_name`   VARCHAR(64)     NOT NULL,
    `description` VARCHAR(255),
    `sort_order`  INT             NOT NULL DEFAULT 0,
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(64),
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) COMMENT='RBAC role definitions';

-- ===== sys_permission: permission point catalog =====
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`          BIGINT          NOT NULL,
    `perm_code`   VARCHAR(64)     NOT NULL,
    `resource`    VARCHAR(32)     NOT NULL,
    `action`      VARCHAR(16)     NOT NULL,
    `description` VARCHAR(255),
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(64),
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`),
    KEY `idx_resource` (`resource`)
) COMMENT='RBAC permission catalog';

-- ===== sys_role_permission: role <-> permission mapping =====
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `role_id`     BIGINT          NOT NULL,
    `perm_id`     BIGINT          NOT NULL,
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`role_id`, `perm_id`)
) COMMENT='RBAC role-permission mapping';

-- ===== sys_user_role: user <-> role mapping (1 user can hold N roles) =====
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id`     BIGINT          NOT NULL,
    `role_id`     BIGINT          NOT NULL,
    `grant_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `grant_by`    VARCHAR(64),
    PRIMARY KEY (`user_id`, `role_id`)
) COMMENT='RBAC user-role mapping';

-- ===== sys_user_data_scope: per-user data visibility =====
CREATE TABLE IF NOT EXISTS `sys_user_data_scope` (
    `user_id`     BIGINT          NOT NULL,
    `scope_type`  VARCHAR(16)     NOT NULL COMMENT 'GLOBAL/PROVINCE/CITY/OWN',
    `scope_value` VARCHAR(64)              COMMENT 'e.g. 山东 or 烟台 or NULL for GLOBAL/OWN',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) COMMENT='RBAC data-scope per user';

-- =============================================================================
-- Seed: 7 roles (6 business + ADMIN)
-- =============================================================================
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `sort_order`) VALUES
(1, 'ADMIN',     '系统管理员',      '平台运营方，全部权限',                            1),
(2, 'FARMER',    '种植户/合作社',   '果园档案、作业记录、农资采购、供应发布、种植贷',  10),
(3, 'SUPPLIER',  '供应商/加工方',   '加工溯源、质检上传、仓储入库',                    20),
(4, 'BUYER',     '收购商/经销商',   '需求发布、撮合对接、线上交易、交易贷/出口贷',     30),
(5, 'LOGISTICS', '物流服务商',      '资源入驻、调度接单、温控追踪',                    40),
(6, 'FINANCE',   '金融机构',        '信用查询、融资审核、放款、风控监控',              50),
(7, 'GOV',       '监管部门',        '全产业链数据查询、异常追溯、统计分析、处罚记录',  60);

-- =============================================================================
-- Seed: permission catalog (resource:action)
-- =============================================================================
INSERT INTO `sys_permission` (`id`, `perm_code`, `resource`, `action`, `description`) VALUES
-- orchard / farm
(100, 'orchard:read',   'orchard', 'read',   '果园档案查看'),
(101, 'orchard:write',  'orchard', 'write',  '果园档案编辑'),
(102, 'orchard:delete', 'orchard', 'delete', '果园档案删除'),
-- cultivation
(110, 'cultivation:read',  'cultivation', 'read',  '种植/作业记录查看'),
(111, 'cultivation:write', 'cultivation', 'write', '种植/作业记录编辑'),
-- input (agri-input)
(120, 'input:read',   'input', 'read',   '农资查看'),
(121, 'input:write',  'input', 'write',  '农资采购'),
-- supply / trade
(130, 'supply:read',  'supply', 'read',  '供应信息查看'),
(131, 'supply:write', 'supply', 'write', '供应信息发布'),
(140, 'trade:read',    'trade', 'read',    '订单查看'),
(141, 'trade:write',   'trade', 'write',   '订单创建/编辑'),
(142, 'trade:approve', 'trade', 'approve', '订单审批/确认'),
-- warehouse
(150, 'warehouse:read',  'warehouse', 'read',  '仓储查看'),
(151, 'warehouse:write', 'warehouse', 'write', '仓储入库/出库'),
-- coldchain / logistics
(160, 'logistics:read',  'logistics', 'read',  '物流查看'),
(161, 'logistics:write', 'logistics', 'write', '物流调度'),
-- finance
(170, 'finance:read',    'finance', 'read',    '金融数据查看'),
(171, 'finance:write',   'finance', 'write',   '融资申请/审核'),
(172, 'finance:approve', 'finance', 'approve', '融资放款'),
-- trace
(180, 'trace:read',  'trace', 'read',  '溯源查看'),
(181, 'trace:write', 'trace', 'write', '溯源记录上传'),
-- regulator / stats
(190, 'stats:read',     'stats',     'read',  '统计分析查看'),
(191, 'penalty:write',  'penalty',   'write', '处罚记录登记'),
-- admin / user mgmt
(900, 'user:read',   'user', 'read',   '用户查看'),
(901, 'user:write',  'user', 'write',  '用户编辑'),
(902, 'user:delete', 'user', 'delete', '用户删除'),
(910, 'role:read',   'role', 'read',   '角色查看'),
(911, 'role:write',  'role', 'write',  '角色与权限分配');

-- =============================================================================
-- Seed: role-permission grants
-- =============================================================================

-- ADMIN: all permissions
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`)
SELECT 1, id FROM `sys_permission`;

-- FARMER: orchard, cultivation, input, supply (write), finance (read+write self)
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(2, 100), (2, 101),
(2, 110), (2, 111),
(2, 120), (2, 121),
(2, 130), (2, 131),
(2, 140), (2, 141),
(2, 170), (2, 171),
(2, 180), (2, 181);

-- SUPPLIER: trace write, warehouse, supply
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(3, 130), (3, 131),
(3, 150), (3, 151),
(3, 180), (3, 181),
(3, 110), (3, 111);

-- BUYER: trade RW + approve, supply read, finance read+write (trade-loan/export-loan)
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(4, 130),
(4, 140), (4, 141), (4, 142),
(4, 170), (4, 171),
(4, 180);

-- LOGISTICS: logistics RW, warehouse read, trace read
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(5, 160), (5, 161),
(5, 150),
(5, 180);

-- FINANCE: finance read+approve, trade read, stats read
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(6, 170), (6, 171), (6, 172),
(6, 140),
(6, 190);

-- GOV / regulator: read all + penalty write + stats
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(7, 100), (7, 110), (7, 120), (7, 130),
(7, 140), (7, 150), (7, 160), (7, 170),
(7, 180),
(7, 190), (7, 191);

-- =============================================================================
-- Seed: user-role mapping for existing seeded users (uc_user from V4)
-- =============================================================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `grant_by`) VALUES
(1001, 1, 'system'),  -- admin    -> ADMIN
(1002, 2, 'system'),  -- farmer01 -> FARMER
(1003, 4, 'system');  -- buyer01  -> BUYER

-- Default data scopes
INSERT INTO `sys_user_data_scope` (`user_id`, `scope_type`, `scope_value`) VALUES
(1001, 'GLOBAL',  NULL),
(1002, 'OWN',     NULL),
(1003, 'OWN',     NULL);


-- ============================================
-- V13__maturity_tables.sql
-- ============================================
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


-- ============================================
-- V14__trace_code_table.sql
-- ============================================
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


-- ============================================
-- V15__orchard_geo.sql
-- ============================================
-- =============================================================================
-- V15 - M4 GIS map: orchard geo fields (center point, polygon boundary, area)
-- Reuses existing pt_orchard.longitude/latitude as the marker; adds polygon
-- boundary as JSON (MySQL 8 native JSON type) plus a derived area in mu.
--
-- Decisions vs spec:
--   1. Use JSON type instead of MySQL spatial GEOMETRY because:
--      - Existing schema is plain InnoDB, no SRID setup
--      - Frontend produces GeoJSON natively (高德 polygon → GeoJSON)
--      - Bbox queries are still performant with the derived center point + index
--   2. center_lat/center_lng are denormalized from boundary centroid for
--      O(1) bbox queries without parsing JSON each time
--   3. area_mu is computed server-side via Shoelace formula on save (亩 = 666.67 m²)
-- =============================================================================

ALTER TABLE `pt_orchard`
    ADD COLUMN `latitude`          DECIMAL(10,7) NULL COMMENT '纬度' AFTER `area`,
    ADD COLUMN `longitude`         DECIMAL(10,7) NULL COMMENT '经度' AFTER `latitude`,
    ADD COLUMN `center_lat`        DECIMAL(10,7) NULL COMMENT 'GIS 中心点纬度（边界质心）' AFTER `longitude`,
    ADD COLUMN `center_lng`        DECIMAL(10,7) NULL COMMENT 'GIS 中心点经度（边界质心）' AFTER `center_lat`,
    ADD COLUMN `boundary_geojson`  JSON          NULL COMMENT '地块边界 GeoJSON Polygon' AFTER `center_lng`,
    ADD COLUMN `area_mu`           DECIMAL(12,2) NULL COMMENT '由边界自动计算的面积（亩）' AFTER `boundary_geojson`;

-- Index for bbox/viewport queries (center point lookup)
CREATE INDEX `idx_pt_orchard_center` ON `pt_orchard` (`center_lat`, `center_lng`);


-- ============================================
-- V16__chain_record.sql
-- ============================================
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


-- ============================================
-- V17__task_template.sql
-- ============================================
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


-- ============================================
-- V18__task_plan.sql
-- ============================================
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


-- ============================================
-- V19__trade_match.sql
-- ============================================
-- =============================================================================
-- V19 - M7 Matching + Negotiation + Chat
--
-- Three tables:
--   td_trade_match       - candidate matches generated by the algorithm
--   td_trade_negotiation - bilateral price negotiations on top of a match
--   td_chat_message      - messages exchanged on a negotiation (or freeform)
--
-- Decisions:
--   1. UNIQUE (supply_id, demand_id) on td_trade_match prevents duplicate
--      candidates if the algorithm reruns; ON DUPLICATE updates the score.
--   2. td_chat_message uses string session_id (e.g. "negotiation-123") so
--      future channel types (user-DM) can reuse the same table.
--   3. msg_type allows TEXT / PRICE_OFFER / IMAGE / CONTRACT for forward
--      compatibility; only TEXT and PRICE_OFFER are emitted by M7.
-- =============================================================================

CREATE TABLE IF NOT EXISTS `td_trade_match` (
    `id`           BIGINT       NOT NULL,
    `supply_id`    BIGINT       NOT NULL,
    `demand_id`    BIGINT       NOT NULL,
    `match_score`  DECIMAL(5,2) NOT NULL,
    `match_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '0=候选 1=已发起沟通 2=议价中 3=已成交 4=已拒绝',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(64),
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supply_demand` (`supply_id`, `demand_id`),
    KEY `idx_supply_score` (`supply_id`, `match_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M7 撮合候选';

CREATE TABLE IF NOT EXISTS `td_trade_negotiation` (
    `id`              BIGINT       NOT NULL,
    `match_id`        BIGINT       NOT NULL,
    `supply_user_id`  BIGINT       NOT NULL,
    `demand_user_id`  BIGINT       NOT NULL,
    `current_price`   DECIMAL(10,2),
    `current_quantity` DECIMAL(12,2),
    `last_offer_by`   BIGINT                COMMENT '上一次报价的用户id',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0=进行中 1=已达成 2=已取消',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`       VARCHAR(64),
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_match` (`match_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M7 议价会话';

CREATE TABLE IF NOT EXISTS `td_chat_message` (
    `id`           BIGINT       NOT NULL,
    `session_id`   VARCHAR(64)  NOT NULL COMMENT 'negotiation-{id} 或 user-{a}-{b}',
    `from_user_id` BIGINT       NOT NULL,
    `to_user_id`   BIGINT       NOT NULL,
    `msg_type`     VARCHAR(16)  NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT/PRICE_OFFER/IMAGE/CONTRACT',
    `content`      TEXT         NOT NULL,
    `send_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `read_flag`    TINYINT      NOT NULL DEFAULT 0,
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_session_time` (`session_id`, `send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M7 聊天消息';


-- ============================================
-- V20__finance_gateway.sql
-- ============================================
-- =============================================================================
-- V20 - M8 Contract + Payment + Invoice
--
-- Three tables, all keyed by td_trade_order.id and unique on order_id
-- (one contract / one payment-attempt / one invoice per order in v1).
-- =============================================================================

CREATE TABLE IF NOT EXISTS `td_trade_contract` (
    `id`            BIGINT       NOT NULL,
    `order_id`      BIGINT       NOT NULL,
    `contract_no`   VARCHAR(64)  NOT NULL COMMENT 'gateway-issued contract number',
    `template_id`   VARCHAR(64),
    `party_a`       BIGINT,
    `party_b`       BIGINT,
    `party_a_signed` TINYINT     NOT NULL DEFAULT 0,
    `party_a_sign_time` DATETIME,
    `party_b_signed` TINYINT     NOT NULL DEFAULT 0,
    `party_b_sign_time` DATETIME,
    `contract_file_url` VARCHAR(512),
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0=draft 1=pending-sign 2=signed 3=canceled',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(64),
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order` (`order_id`),
    UNIQUE KEY `uk_contract_no` (`contract_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M8 电子合同';

CREATE TABLE IF NOT EXISTS `td_trade_payment` (
    `id`             BIGINT       NOT NULL,
    `order_id`       BIGINT       NOT NULL,
    `payment_no`     VARCHAR(64)  NOT NULL,
    `channel`        VARCHAR(16)  NOT NULL COMMENT 'WECHAT/UNION_PAY/BANK_TRANSFER',
    `amount`         DECIMAL(12,2) NOT NULL,
    `third_trade_no` VARCHAR(128),
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0=待支付 1=支付中 2=成功 3=失败 4=退款',
    `pay_time`       DATETIME,
    `callback_time`  DATETIME,
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M8 支付';

CREATE TABLE IF NOT EXISTS `td_trade_invoice` (
    `id`            BIGINT       NOT NULL,
    `order_id`      BIGINT       NOT NULL,
    `payment_id`    BIGINT       NOT NULL,
    `invoice_no`    VARCHAR(64)  NOT NULL,
    `invoice_code`  VARCHAR(32),
    `tax_payer`     VARCHAR(128),
    `tax_no`        VARCHAR(32),
    `amount`        DECIMAL(12,2) NOT NULL,
    `tax_amount`    DECIMAL(12,2),
    `pdf_url`       VARCHAR(512),
    `issue_time`    DATETIME,
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0=申请中 1=成功 2=失败',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(64),
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invoice_no` (`invoice_no`),
    KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M8 发票';


-- ============================================
-- V21__iot_tables.sql
-- ============================================
-- =============================================================================
-- V21 - M3 5G+北斗 IoT mock + 告警
--
-- Three tables. iot_telemetry uses (device_sn, collect_time) as a unique key
-- to make the ingest endpoint idempotent (replays from edge gateway during
-- reconnect cycles).
-- =============================================================================

CREATE TABLE IF NOT EXISTS `iot_device` (
    `id`                BIGINT       NOT NULL,
    `device_sn`         VARCHAR(64)  NOT NULL,
    `device_type`       VARCHAR(16)  NOT NULL COMMENT 'COLD_TRUCK / WAREHOUSE / PRE_COOLING',
    `vehicle_no`        VARCHAR(16)           COMMENT '关联车牌号',
    `warehouse_id`      BIGINT,
    `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '0=离线 1=在线 2=故障',
    `last_online_time`  DATETIME,
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`         VARCHAR(64),
    `deleted`           TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_sn` (`device_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M3 IoT 设备';

CREATE TABLE IF NOT EXISTS `iot_telemetry` (
    `id`             BIGINT       NOT NULL,
    `device_sn`      VARCHAR(64)  NOT NULL,
    `collect_time`   DATETIME     NOT NULL,
    `latitude`       DECIMAL(10,7),
    `longitude`      DECIMAL(10,7),
    `altitude`       DECIMAL(8,2),
    `speed`          DECIMAL(6,2) COMMENT 'km/h',
    `temp_1`         DECIMAL(5,2),
    `temp_2`         DECIMAL(5,2),
    `temp_3`         DECIMAL(5,2),
    `temp_4`         DECIMAL(5,2),
    `humidity`       DECIMAL(5,2),
    `door_status`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0=关 1=开',
    `signal_type`    VARCHAR(8)   COMMENT '5G/4G/OFFLINE',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_time` (`device_sn`, `collect_time`),
    KEY `idx_device_time` (`device_sn`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M3 IoT 遥测';
-- Production note: partition by RANGE (TO_DAYS(collect_time)) when row count > 10M.

CREATE TABLE IF NOT EXISTS `iot_alert` (
    `id`             BIGINT       NOT NULL,
    `device_sn`      VARCHAR(64),
    `alert_type`     VARCHAR(32)  NOT NULL COMMENT 'TEMP_HIGH/TEMP_LOW/DOOR_ANOMALY/OFFLINE/SENSOR_FAULT',
    `alert_level`    TINYINT      NOT NULL DEFAULT 2 COMMENT '1=提醒 2=警告 3=严重',
    `threshold_value` VARCHAR(32),
    `actual_value`   VARCHAR(32),
    `message`        VARCHAR(512),
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `handled`        TINYINT      NOT NULL DEFAULT 0,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_device_type` (`device_sn`, `alert_type`),
    KEY `idx_handled` (`handled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M3 IoT 告警';


-- ============================================
-- V22__chain_record_seed.sql
-- ============================================
-- =============================================================================
-- V22 - Seed data for tr_chain_record (M2 blockchain certification)
--
-- 5 records matching the trace chains in tr_trace_chain (V2 seed):
--   4001: SOLD       -> chain_status=1 (success), hash verified
--   4002: IN_TRANSIT -> chain_status=1 (success), hash verified
--   4003: HARVESTED  -> chain_status=0 (pending)
--   4004: IN_STORAGE -> chain_status=2 (failed), 2 retries, timeout error
--   4005: PLANTED    -> chain_status=3 (retrying), 1 retry, 503 error
--
-- NOTE: data_hash values are SHA-256 of the MySQL-stored JSON form
-- (MySQL JSON columns reorder keys alphabetically). These hashes match
-- exactly what the Java verifyByTraceCode endpoint will re-compute.
-- =============================================================================

-- 1. SOLD -> success
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4001, 'TC20260301001', 'BATCH', 3001,
    '{"batchNo": "TB20260301001", "orchardId": 1001, "status": "SOLD", "variety": "红富士"}',
    '05979363b1474c0862a3239ffd54a93f2e9c83e056d421a209447d8ae4dc0c70',
    'tx_oulink_7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8',
    1000001, 1, NULL, 0,
    '2026-03-01 10:30:00', '2026-03-01 10:30:01', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 2. IN_TRANSIT -> success
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4002, 'TC20260301002', 'BATCH', 3002,
    '{"batchNo": "TB20260301002", "orchardId": 1002, "status": "IN_TRANSIT", "variety": "嘎拉"}',
    '82d0df55e53ddfdb18ecf8e527159d5cce7e6f0212a7f8a458ec56aea46476c7',
    'tx_oulink_8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0c1d2e3f4g5h6i7j8k9',
    1000002, 1, NULL, 0,
    '2026-03-01 14:20:00', '2026-03-01 14:20:02', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 3. HARVESTED -> pending (waiting to upload)
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4003, 'TC20260302001', 'BATCH', 3003,
    '{"batchNo": "TB20260302001", "orchardId": 1003, "status": "HARVESTED", "variety": "黄元帅"}',
    '84110044aa646b288e931f5b611b654a739fb4b2751c0dd7bdd0686436ab28ea',
    NULL, NULL, 0, NULL, 0,
    '2026-03-02 09:15:00', '2026-03-02 09:15:00', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 4. IN_STORAGE -> failed (after 2 retries)
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4004, 'TC20260303001', 'BATCH', 3004,
    '{"batchNo": "TB20260303001", "orchardId": 1001, "status": "IN_STORAGE", "variety": "秦冠"}',
    '699ea96fde518ad9be55a2657cd4d72200de4dc6845881a965b4033984ccf2ff',
    NULL, NULL, 2, '连接奥链超时: Connection timed out', 2,
    '2026-03-03 11:00:00', '2026-03-03 11:05:00', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 5. PLANTED -> retrying
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4005, 'TC20260304001', 'BATCH', 3005,
    '{"batchNo": "TB20260304001", "orchardId": 1002, "status": "PLANTED", "variety": "红富士"}',
    '9ca6fc547e63fcc132808e2b886e50853cd3c8507d1f6e22f8fdd4a28c0fa5d7',
    NULL, NULL, 3, '奥链节点响应异常: HTTP 503', 1,
    '2026-03-04 16:45:00', '2026-03-04 16:50:00', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;


-- ============================================
-- V23__core_business_seed.sql
-- ============================================
-- =============================================================================
-- V23 - Seed data for 5 P0 empty tables
--
-- Tables populated:
--   1. cc_pre_cool_task    — Precooling management (PENDING/COOLING/COMPLETED/FAILED)
--   2. trace_code          — Three-level traceability codes (BATCH/BOX/FRUIT)
--   3. pt_harvest_batch    — Harvest batches
--   4. pt_maturity_record  — Maturity sampling records (UNRIPE/OPTIMAL/OVERRIPE)
--   5. pt_task_plan        — AI task plans (PENDING/IN_PROGRESS/DONE/SKIPPED)
--
-- References existing seed data from V2:
--   farm_orchard: 20001-20005
--   trace_batch:  30001-30005 (batch_code: TB202510010001..005)
--   cc_vehicle:   8001-8004
-- =============================================================================

-- ========== 1. cc_pre_cool_task (4 rows, all statuses) ==========

INSERT INTO cc_pre_cool_task (id, task_no, vehicle_id, batch_code, start_temp, target_temp,
    start_time, end_time, duration, status, operator, remark,
    create_time, update_time, create_by, deleted)
VALUES
(6001, 'PCT20260301001', 8001, 'TB202510010001', 22.50, 2.00,
    '2026-03-01 08:00:00', '2026-03-01 10:30:00', 150, 'COMPLETED', '刘冷链', '红富士批次预冷完成，温度达标',
    '2026-03-01 07:30:00', '2026-03-01 10:30:00', 'system', 0),
(6002, 'PCT20260302001', 8002, 'TB202510010002', 19.80, 2.00,
    '2026-03-02 09:00:00', NULL, NULL, 'COOLING', '王冷运', '嘎拉批次预冷进行中',
    '2026-03-02 08:45:00', '2026-03-02 09:00:00', 'system', 0),
(6003, 'PCT20260303001', 8003, 'TB202510010003', 24.10, 2.00,
    NULL, NULL, NULL, 'PENDING', '刘冷链', '黄元帅批次待预冷，等待入库',
    '2026-03-03 07:00:00', '2026-03-03 07:00:00', 'system', 0),
(6004, 'PCT20260304001', 8001, 'TB202510010004', 21.30, 2.00,
    '2026-03-04 06:00:00', '2026-03-04 08:00:00', 120, 'FAILED', '王冷运', '设备故障导致预冷中断，需重新操作',
    '2026-03-04 05:30:00', '2026-03-04 08:00:00', 'system', 0)
ON DUPLICATE KEY UPDATE task_no = task_no;

-- ========== 2. trace_code (BATCH×5 + BOX×3 + FRUIT×6 = 14 rows) ==========

-- BATCH level: one code per trace_batch
INSERT INTO trace_code (id, code, granularity, parent_code, batch_id, crc16, qr_url, status,
    create_time, update_time, create_by, deleted)
VALUES
(7001, 'TC-B-TB202510010001', 'BATCH', NULL, 30001, 'A1B2', 'https://trace.apple-chain.com/q/TC-B-TB202510010001', 'ACTIVE',
    '2026-03-01 10:00:00', '2026-03-01 10:00:00', 'system', 0),
(7002, 'TC-B-TB202510010002', 'BATCH', NULL, 30002, 'C3D4', 'https://trace.apple-chain.com/q/TC-B-TB202510010002', 'ACTIVE',
    '2026-03-01 10:00:00', '2026-03-01 10:00:00', 'system', 0),
(7003, 'TC-B-TB202510010003', 'BATCH', NULL, 30003, 'E5F6', 'https://trace.apple-chain.com/q/TC-B-TB202510010003', 'ACTIVE',
    '2026-03-02 09:00:00', '2026-03-02 09:00:00', 'system', 0),
(7004, 'TC-B-TB202510010004', 'BATCH', NULL, 30004, '7890', 'https://trace.apple-chain.com/q/TC-B-TB202510010004', 'VOID',
    '2026-03-02 09:00:00', '2026-03-05 14:00:00', 'system', 0),
(7005, 'TC-B-TB202510010005', 'BATCH', NULL, 30005, '1A2B', 'https://trace.apple-chain.com/q/TC-B-TB202510010005', 'ACTIVE',
    '2026-03-03 08:00:00', '2026-03-03 08:00:00', 'system', 0)
ON DUPLICATE KEY UPDATE code = code;

-- BOX level: 3 boxes under batch 30001
INSERT INTO trace_code (id, code, granularity, parent_code, batch_id, crc16, qr_url, status,
    create_time, update_time, create_by, deleted)
VALUES
(7011, 'TC-X-TB202510010001-01', 'BOX', 'TC-B-TB202510010001', 30001, '2B3C', 'https://trace.apple-chain.com/q/TC-X-TB202510010001-01', 'ACTIVE',
    '2026-03-01 11:00:00', '2026-03-01 11:00:00', 'system', 0),
(7012, 'TC-X-TB202510010001-02', 'BOX', 'TC-B-TB202510010001', 30001, '4D5E', 'https://trace.apple-chain.com/q/TC-X-TB202510010001-02', 'CONSUMED',
    '2026-03-01 11:00:00', '2026-03-10 16:00:00', 'system', 0),
(7013, 'TC-X-TB202510010001-03', 'BOX', 'TC-B-TB202510010001', 30001, '6F70', 'https://trace.apple-chain.com/q/TC-X-TB202510010001-03', 'ACTIVE',
    '2026-03-01 11:00:00', '2026-03-01 11:00:00', 'system', 0)
ON DUPLICATE KEY UPDATE code = code;

-- FRUIT level: 6 fruits across boxes
INSERT INTO trace_code (id, code, granularity, parent_code, batch_id, crc16, qr_url, status,
    create_time, update_time, create_by, deleted)
VALUES
(7021, 'TC-F-TB202510010001-01-001', 'FRUIT', 'TC-X-TB202510010001-01', 30001, 'A1A1', 'https://trace.apple-chain.com/q/TC-F-TB202510010001-01-001', 'ACTIVE',
    '2026-03-01 12:00:00', '2026-03-01 12:00:00', 'system', 0),
(7022, 'TC-F-TB202510010001-01-002', 'FRUIT', 'TC-X-TB202510010001-01', 30001, 'B2B2', 'https://trace.apple-chain.com/q/TC-F-TB202510010001-01-002', 'ACTIVE',
    '2026-03-01 12:00:00', '2026-03-01 12:00:00', 'system', 0),
(7023, 'TC-F-TB202510010001-02-001', 'FRUIT', 'TC-X-TB202510010001-02', 30001, 'C3C3', 'https://trace.apple-chain.com/q/TC-F-TB202510010001-02-001', 'CONSUMED',
    '2026-03-01 12:00:00', '2026-03-10 16:00:00', 'system', 0),
(7024, 'TC-F-TB202510010001-02-002', 'FRUIT', 'TC-X-TB202510010001-02', 30001, 'D4D4', 'https://trace.apple-chain.com/q/TC-F-TB202510010001-02-002', 'CONSUMED',
    '2026-03-01 12:00:00', '2026-03-10 16:00:00', 'system', 0),
(7025, 'TC-F-TB202510010001-03-001', 'FRUIT', 'TC-X-TB202510010001-03', 30001, 'E5E5', 'https://trace.apple-chain.com/q/TC-F-TB202510010001-03-001', 'ACTIVE',
    '2026-03-01 12:00:00', '2026-03-01 12:00:00', 'system', 0),
(7026, 'TC-F-TB202510010001-03-002', 'FRUIT', 'TC-X-TB202510010001-03', 30001, 'F6F6', 'https://trace.apple-chain.com/q/TC-F-TB202510010001-03-002', 'ACTIVE',
    '2026-03-01 12:00:00', '2026-03-01 12:00:00', 'system', 0)
ON DUPLICATE KEY UPDATE code = code;

-- ========== 3. pt_harvest_batch (5 rows, all statuses) ==========

INSERT INTO pt_harvest_batch (id, orchard_id, total_weight, harvest_date, status, deleted)
VALUES
(5001, 20001, 45000.00, '2026-03-01', 'DRAFT', 0),
(5002, 20002, 28000.00, '2026-03-01', 'CONFIRMED', 0),
(5003, 20003, 62000.00, '2026-03-02', 'IN_STORAGE', 0),
(5004, 20005, 38000.00, '2026-03-03', 'CONFIRMED', 0),
(5005, 20001, 51000.00, '2026-03-04', 'DRAFT', 0)
ON DUPLICATE KEY UPDATE id = id;

-- ========== 4. pt_maturity_record (6 rows, UNRIPE/OPTIMAL/OVERRIPE) ==========

INSERT INTO pt_maturity_record (id, orchard_id, variety, sample_date, brix, firmness,
    color_rgb, accumulate_temp, maturity_score, recommendation, operator, remark,
    create_time, update_time, create_by, deleted)
VALUES
(10001, 20001, '红富士', '2026-03-01', 14.20, 7.80, 'C8281E', 3100, 82.50, 'OPTIMAL', '张大农', '糖度硬度均达标，建议采收',
    '2026-03-01 09:00:00', '2026-03-01 09:00:00', 'system', 0),
(10002, 20002, '嘎拉', '2026-03-01', 10.50, 6.20, 'D94A38', 2100, 55.00, 'UNRIPE', '李富贵', '糖度偏低，建议再等一周',
    '2026-03-01 10:30:00', '2026-03-01 10:30:00', 'system', 0),
(10003, 20003, '黄元帅', '2026-03-02', 13.80, 5.60, 'E8D547', 2900, 88.00, 'OPTIMAL', '张大农', '黄元帅完全成熟，品质优秀',
    '2026-03-02 08:00:00', '2026-03-02 08:00:00', 'system', 0),
(10004, 20001, '红富士', '2026-03-05', 16.50, 5.20, 'B81E0E', 3500, 72.00, 'OVERRIPE', '张大农', '过度成熟，硬度下降需尽快处理',
    '2026-03-05 09:00:00', '2026-03-05 09:00:00', 'system', 0),
(10005, 20005, '秦冠', '2026-03-03', 12.80, 7.10, 'A5201A', 2800, 75.00, 'OPTIMAL', '赵铁柱', '秦冠成熟度适中',
    '2026-03-03 10:00:00', '2026-03-03 10:00:00', 'system', 0),
(10006, 20004, '红富士', '2026-03-04', 9.80, 8.50, 'D45030', 1800, 35.00, 'UNRIPE', '王花花', '果园已停用，样品仅供对比',
    '2026-03-04 11:00:00', '2026-03-04 11:00:00', 'system', 0)
ON DUPLICATE KEY UPDATE id = id;

-- ========== 5. pt_task_plan (8 rows, all statuses) ==========

INSERT INTO pt_task_plan (id, orchard_id, template_id, operation_type, task_name,
    plan_date, priority, material_suggestion, status, actual_operation_id, generated_by, remark,
    create_time, update_time, create_by, deleted)
VALUES
(11001, 20001, NULL, 'FERTILIZE', '春季基肥施用',
    '2026-03-10', 2, '有机肥 200kg/亩 + 复合肥 50kg/亩', 'DONE', NULL, 'AUTO', 'AI 根据物候期自动生成',
    '2026-03-01 08:00:00', '2026-03-10 16:00:00', 'system', 0),
(11002, 20001, NULL, 'PRUNE', '冬季修剪整形',
    '2026-03-15', 3, '修剪锯、伤口愈合剂', 'IN_PROGRESS', NULL, 'AUTO', '修剪过密枝条，改善通风',
    '2026-03-01 08:00:00', '2026-03-15 09:00:00', 'system', 0),
(11003, 20002, NULL, 'PESTICIDE', '早春病虫害防治',
    '2026-03-08', 1, '石硫合剂 5度', 'DONE', NULL, 'AUTO', '萌芽前喷施石硫合剂',
    '2026-03-01 08:00:00', '2026-03-08 15:00:00', 'system', 0),
(11004, 20002, NULL, 'IRRIGATE', '萌芽水灌溉',
    '2026-03-12', 3, NULL, 'PENDING', NULL, 'AUTO', '土壤含水量低于 60% 时灌溉',
    '2026-03-01 08:00:00', '2026-03-01 08:00:00', 'system', 0),
(11005, 20003, NULL, 'THIN', '疏花疏果',
    '2026-04-01', 4, NULL, 'PENDING', NULL, 'AUTO', '根据座果率确定疏果量',
    '2026-03-01 08:00:00', '2026-03-01 08:00:00', 'system', 0),
(11006, 20005, NULL, 'FERTILIZE', '追肥 - 膨大期',
    '2026-03-20', 2, '钾肥 30kg/亩', 'PENDING', NULL, 'AUTO', '果实膨大期追施钾肥',
    '2026-03-01 08:00:00', '2026-03-01 08:00:00', 'system', 0),
(11007, 20001, NULL, 'PESTICIDE', '蚜虫紧急防治',
    '2026-03-18', 1, '吡虫啉 2000 倍液', 'SKIPPED', NULL, 'MANUAL', '果农手动添加，后发现虫害不严重已跳过',
    '2026-03-15 10:00:00', '2026-03-18 14:00:00', 'system', 0),
(11008, 20003, NULL, 'HARVEST', '秋季采收计划',
    '2026-09-15', 5, '采果袋、周转箱', 'PENDING', NULL, 'AUTO', '预计 9 月中旬成熟',
    '2026-03-01 08:00:00', '2026-03-01 08:00:00', 'system', 0)
ON DUPLICATE KEY UPDATE id = id;


-- ============================================
-- V24__harvest_batch_alter.sql
-- ============================================
-- =============================================================================
-- V24 - Add missing columns to pt_harvest_batch
--
-- The V4 DDL created a minimal table (id, orchard_id, total_weight, harvest_date,
-- status, deleted) but the Java entity HarvestBatch expects additional columns.
-- This migration adds the missing columns to match the entity definition.
-- =============================================================================

ALTER TABLE pt_harvest_batch
    ADD COLUMN batch_no       VARCHAR(32)           COMMENT 'Unique batch number: HB+yyyyMMdd+seq',
    ADD COLUMN grade_a        DECIMAL(12,2)         COMMENT 'Grade A weight (kg)',
    ADD COLUMN grade_b        DECIMAL(12,2)         COMMENT 'Grade B weight (kg)',
    ADD COLUMN grade_c        DECIMAL(12,2)         COMMENT 'Grade C weight (kg)',
    ADD COLUMN trace_code     VARCHAR(64)           COMMENT 'Linked traceability code',
    ADD COLUMN remark         VARCHAR(512)          COMMENT 'Notes',
    ADD COLUMN create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN create_by      VARCHAR(64);

-- Update seed data to populate the new columns
UPDATE pt_harvest_batch SET
    batch_no = 'HB20260301001',
    grade_a = 30000.00,
    grade_b = 10000.00,
    grade_c = 5000.00,
    remark = '红富士张家湾第一批',
    create_time = '2026-03-01 08:00:00',
    update_time = '2026-03-01 08:00:00',
    create_by = 'system'
WHERE id = 5001;

UPDATE pt_harvest_batch SET
    batch_no = 'HB20260301002',
    grade_a = 15000.00,
    grade_b = 8000.00,
    grade_c = 5000.00,
    trace_code = 'TC-B-TB202510010002',
    remark = '嘎拉李家沟已确认',
    create_time = '2026-03-01 09:00:00',
    update_time = '2026-03-01 12:00:00',
    create_by = 'system'
WHERE id = 5002;

UPDATE pt_harvest_batch SET
    batch_no = 'HB20260302001',
    grade_a = 40000.00,
    grade_b = 15000.00,
    grade_c = 7000.00,
    trace_code = 'TC-B-TB202510010003',
    remark = '黄元帅金苹果已入库',
    create_time = '2026-03-02 07:00:00',
    update_time = '2026-03-02 14:00:00',
    create_by = 'system'
WHERE id = 5003;

UPDATE pt_harvest_batch SET
    batch_no = 'HB20260303001',
    grade_a = 20000.00,
    grade_b = 10000.00,
    grade_c = 8000.00,
    trace_code = 'TC-B-TB202510010004',
    remark = '秦冠绿野已确认',
    create_time = '2026-03-03 08:30:00',
    update_time = '2026-03-03 10:00:00',
    create_by = 'system'
WHERE id = 5004;

UPDATE pt_harvest_batch SET
    batch_no = 'HB20260304001',
    grade_a = 35000.00,
    grade_b = 12000.00,
    grade_c = 4000.00,
    remark = '红富士张家湾第二批',
    create_time = '2026-03-04 07:30:00',
    update_time = '2026-03-04 07:30:00',
    create_by = 'system'
WHERE id = 5005;


-- ============================================
-- V25__quality_inspection.sql
-- ============================================
-- V25: Quality inspection table for trade module
CREATE TABLE IF NOT EXISTS td_quality_inspection (
    id              BIGINT          NOT NULL    PRIMARY KEY,
    inspection_no   VARCHAR(32)     NULL,
    order_id        BIGINT          NULL,
    supply_id       BIGINT          NULL,
    variety         VARCHAR(64)     NULL,
    grade           VARCHAR(4)      NULL,
    brix_value      DECIMAL(6,2)    NULL        COMMENT '糖度 Brix',
    firmness_value  DECIMAL(6,2)    NULL        COMMENT '硬度',
    color_score     DECIMAL(6,2)    NULL        COMMENT '色泽评分',
    defect_rate     DECIMAL(6,2)    NULL        COMMENT '缺陷率 %',
    inspector_name  VARCHAR(64)     NULL,
    inspection_date DATE            NULL,
    report_url      VARCHAR(512)    NULL,
    result          VARCHAR(16)     NULL        COMMENT 'PASS/FAIL/CONDITIONAL',
    status          VARCHAR(16)     NOT NULL    DEFAULT 'PENDING'  COMMENT 'PENDING/INSPECTED/ACCEPTED/DISPUTED',
    remark          VARCHAR(512)    NULL,
    create_time     DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(64)     NULL,
    deleted         TINYINT         NOT NULL    DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质量检验表';


-- ============================================
-- V26__anomaly_trace.sql
-- ============================================
-- V26: Anomaly trace table for trace module
CREATE TABLE IF NOT EXISTS tr_anomaly_trace (
    id                      BIGINT          NOT NULL    PRIMARY KEY,
    trace_code              VARCHAR(64)     NULL,
    batch_id                BIGINT          NULL,
    anomaly_type            VARCHAR(32)     NULL        COMMENT 'PESTICIDE_EXCESS/TEMP_VIOLATION/QUALITY_FAIL/OTHER',
    description             VARCHAR(512)    NULL,
    severity                VARCHAR(16)     NULL        COMMENT 'HIGH/MEDIUM/LOW',
    status                  VARCHAR(16)     NOT NULL    DEFAULT 'OPEN'  COMMENT 'OPEN/INVESTIGATING/RESOLVED',
    affected_batch_count    INT             NULL,
    affected_fruit_count    INT             NULL,
    root_cause_analysis     VARCHAR(1024)   NULL,
    resolved_by             VARCHAR(64)     NULL,
    resolved_time           DATETIME        NULL,
    remark                  VARCHAR(512)    NULL,
    create_time             DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by               VARCHAR(64)     NULL,
    deleted                 TINYINT         NOT NULL    DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常追溯表';


