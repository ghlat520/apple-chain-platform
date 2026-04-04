-- =============================================================================
-- V4 (TEST ONLY) - Missing tables required by entity classes and DashboardMapper
-- These tables are not in V1-V3 but are referenced by controllers/mappers.
-- =============================================================================

-- ===== uc_user (User entity, AuthController, DashboardMapper) =====
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
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
);

-- Seed: admin user (BCrypt of "admin123")
INSERT IGNORE INTO `uc_user`
    (`id`, `username`, `password`, `real_name`, `role_code`, `status`, `deleted`)
VALUES
    (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'ADMIN', 1, 0),
    (2, 'farmer01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张大农', 'FARMER', 1, 0);

-- ===== td_supply_info (SupplyInfo entity, SupplyInfoController, DashboardMapper) =====
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

-- ===== td_trade_order (TradeOrder entity, TradeOrderController, DashboardMapper) =====
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

-- ===== td_purchase_need (DashboardMapper supply/demand join) =====
CREATE TABLE IF NOT EXISTS `td_purchase_need` (
    `id`          BIGINT          NOT NULL,
    `variety`     VARCHAR(64),
    `quantity`    DECIMAL(12,2),
    `status`      VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- ===== pt_orchard (DashboardMapper) =====
CREATE TABLE IF NOT EXISTS `pt_orchard` (
    `id`          BIGINT          NOT NULL,
    `orchard_name` VARCHAR(128),
    `orchard_no`  VARCHAR(32),
    `variety`     VARCHAR(64),
    `area`        DECIMAL(10,2),
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
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

-- ===== tr_trace_chain (DashboardMapper) =====
CREATE TABLE IF NOT EXISTS `tr_trace_chain` (
    `id`             BIGINT          NOT NULL,
    `current_status` VARCHAR(32),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);
