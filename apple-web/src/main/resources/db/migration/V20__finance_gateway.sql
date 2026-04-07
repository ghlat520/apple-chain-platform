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
