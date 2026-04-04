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
