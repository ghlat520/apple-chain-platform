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
