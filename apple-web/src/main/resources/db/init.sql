-- =============================================================================
-- 苹果产业链云服务平台 Database Init Script
-- Database: apple_chain
-- Charset: utf8mb4
-- =============================================================================

CREATE DATABASE IF NOT EXISTS apple_chain DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE apple_chain;

-- =============================================================================
-- MODULE: USER (prefix: uc_)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `uc_user` (
    `id`          BIGINT       NOT NULL COMMENT '雪花ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT 'BCrypt密码',
    `real_name`   VARCHAR(64)           COMMENT '真实姓名',
    `phone`       VARCHAR(20)           COMMENT '手机号',
    `email`       VARCHAR(128)          COMMENT '邮箱',
    `role_code`   VARCHAR(32)  NOT NULL COMMENT '角色: FARMER/SUPPLIER/BUYER/WAREHOUSE/LOGISTICS/FINANCE/GOV/ADMIN',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    `org_name`    VARCHAR(128)          COMMENT '机构名称',
    `org_type`    VARCHAR(64)           COMMENT '机构类型',
    `avatar`      VARCHAR(512)          COMMENT '头像URL',
    `remark`      VARCHAR(512)          COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   VARCHAR(64)           COMMENT '创建人',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '软删除: 0正常 1已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`, `deleted`),
    KEY `idx_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台用户表';

-- =============================================================================
-- MODULE: PLANTING (prefix: pt_)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `pt_orchard` (
    `id`          BIGINT          NOT NULL COMMENT '雪花ID',
    `orchard_no`  VARCHAR(32)     NOT NULL COMMENT '果园编号',
    `orchard_name` VARCHAR(128)   NOT NULL COMMENT '果园名称',
    `farmer_id`   BIGINT          NOT NULL COMMENT '农户ID(FK uc_user)',
    `area`        DECIMAL(10,2)            COMMENT '面积(亩)',
    `variety`     VARCHAR(64)              COMMENT '苹果品种',
    `location`    VARCHAR(512)             COMMENT '地址',
    `longitude`   DECIMAL(11,7)            COMMENT '经度',
    `latitude`    DECIMAL(10,7)            COMMENT '纬度',
    `tree_age`    INT                      COMMENT '树龄(年)',
    `status`      VARCHAR(16)     NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/DORMANT/HARVESTED',
    `remark`      VARCHAR(512)             COMMENT '备注',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(64),
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orchard_no` (`orchard_no`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='果园信息表';

CREATE TABLE IF NOT EXISTS `pt_growth_record` (
    `id`           BIGINT       NOT NULL COMMENT '雪花ID',
    `orchard_id`   BIGINT       NOT NULL COMMENT 'FK pt_orchard',
    `record_type`  VARCHAR(32)  NOT NULL COMMENT 'FERTILIZE/SPRAY/IRRIGATE/PRUNE/PEST_CONTROL',
    `operate_date` DATE         NOT NULL COMMENT '操作日期',
    `operator`     VARCHAR(64)           COMMENT '操作人',
    `materials`    TEXT                  COMMENT '投入品JSON',
    `weather`      VARCHAR(64)           COMMENT '天气',
    `notes`        TEXT                  COMMENT '备注',
    `recorded_by`  BIGINT                COMMENT '记录人ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(64),
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_orchard_id` (`orchard_id`),
    KEY `idx_record_type` (`record_type`),
    KEY `idx_operate_date` (`operate_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农事记录表';

CREATE TABLE IF NOT EXISTS `pt_harvest_batch` (
    `id`           BIGINT          NOT NULL COMMENT '雪花ID',
    `batch_no`     VARCHAR(32)     NOT NULL COMMENT '批次号',
    `orchard_id`   BIGINT          NOT NULL COMMENT 'FK pt_orchard',
    `harvest_date` DATE            NOT NULL COMMENT '采收日期',
    `total_weight` DECIMAL(12,2)            COMMENT '总重量(kg)',
    `grade_a`      DECIMAL(12,2)            COMMENT 'A级重量(kg)',
    `grade_b`      DECIMAL(12,2)            COMMENT 'B级重量(kg)',
    `grade_c`      DECIMAL(12,2)            COMMENT 'C级重量(kg)',
    `status`       VARCHAR(16)     NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED/IN_STORAGE',
    `trace_code`   VARCHAR(64)              COMMENT '溯源码',
    `remark`       VARCHAR(512)             COMMENT '备注',
    `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(64),
    `deleted`      TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_no` (`batch_no`),
    KEY `idx_orchard_id` (`orchard_id`),
    KEY `idx_status` (`status`),
    KEY `idx_harvest_date` (`harvest_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采收批次表';

-- =============================================================================
-- MODULE: TRACE (prefix: tr_)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `tr_trace_chain` (
    `id`             BIGINT       NOT NULL COMMENT '雪花ID',
    `trace_code`     VARCHAR(64)  NOT NULL COMMENT '溯源码(唯一)',
    `product_type`   VARCHAR(32)           DEFAULT 'APPLE' COMMENT '产品类型',
    `batch_no`       VARCHAR(32)           COMMENT 'FK pt_harvest_batch.batch_no',
    `orchard_id`     BIGINT                COMMENT 'FK pt_orchard',
    `farmer_id`      BIGINT                COMMENT 'FK uc_user',
    `current_status` VARCHAR(32)           COMMENT 'PLANTED/HARVESTED/IN_STORAGE/IN_TRANSIT/SOLD',
    `data_hash`      VARCHAR(128)          COMMENT 'SHA-256',
    `chain_status`   TINYINT      NOT NULL DEFAULT 0 COMMENT '0=待上链 1=已上链',
    `remark`         VARCHAR(512),
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_trace_code` (`trace_code`),
    KEY `idx_batch_no` (`batch_no`),
    KEY `idx_orchard_id` (`orchard_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溯源链表';

CREATE TABLE IF NOT EXISTS `tr_trace_node` (
    `id`            BIGINT       NOT NULL COMMENT '雪花ID',
    `trace_code`    VARCHAR(64)  NOT NULL COMMENT 'FK tr_trace_chain',
    `node_type`     VARCHAR(32)  NOT NULL COMMENT 'PLANT/GROW/HARVEST/STORAGE/LOGISTICS/TRADE',
    `node_time`     DATETIME     NOT NULL COMMENT '节点时间',
    `operator_id`   BIGINT                COMMENT '操作人ID',
    `operator_name` VARCHAR(64)           COMMENT '操作人姓名',
    `summary`       VARCHAR(256)          COMMENT '摘要',
    `detail`        TEXT                  COMMENT 'JSON详情',
    `location`      VARCHAR(256)          COMMENT '地点',
    `data_hash`     VARCHAR(128)          COMMENT 'SHA-256',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`     VARCHAR(64),
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_trace_code` (`trace_code`),
    KEY `idx_node_time` (`node_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溯源节点表';

-- =============================================================================
-- MODULE: TRADE (prefix: td_)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `td_supply_info` (
    `id`             BIGINT          NOT NULL COMMENT '雪花ID',
    `supply_no`      VARCHAR(32)     NOT NULL COMMENT '供货编号',
    `farmer_id`      BIGINT          NOT NULL COMMENT 'FK uc_user',
    `orchard_id`     BIGINT                   COMMENT 'FK pt_orchard',
    `variety`        VARCHAR(64)              COMMENT '品种',
    `quantity`       DECIMAL(12,2)            COMMENT '数量(kg)',
    `price_expected` DECIMAL(10,2)            COMMENT '期望价格(元/kg)',
    `harvest_date`   DATE                     COMMENT '采收日期',
    `valid_until`    DATE                     COMMENT '有效期',
    `quality`        VARCHAR(8)               COMMENT 'A/B/C',
    `location`       VARCHAR(256)             COMMENT '地点',
    `description`    TEXT                     COMMENT '描述',
    `status`         VARCHAR(16)     NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/MATCHED/CLOSED',
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supply_no` (`supply_no`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_variety` (`variety`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供货信息表';

CREATE TABLE IF NOT EXISTS `td_purchase_need` (
    `id`           BIGINT          NOT NULL COMMENT '雪花ID',
    `need_no`      VARCHAR(32)     NOT NULL COMMENT '需求编号',
    `buyer_id`     BIGINT          NOT NULL COMMENT 'FK uc_user',
    `variety`      VARCHAR(64)              COMMENT '品种',
    `quantity`     DECIMAL(12,2)            COMMENT '数量(kg)',
    `price_max`    DECIMAL(10,2)            COMMENT '最高价格(元/kg)',
    `require_date` DATE                     COMMENT '需求日期',
    `quality`      VARCHAR(8)               COMMENT 'A/B/C',
    `delivery_addr` VARCHAR(256)            COMMENT '收货地址',
    `description`  TEXT                     COMMENT '描述',
    `status`       VARCHAR(16)     NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/MATCHED/CLOSED',
    `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(64),
    `deleted`      TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_need_no` (`need_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_variety` (`variety`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购需求表';

CREATE TABLE IF NOT EXISTS `td_trade_order` (
    `id`             BIGINT          NOT NULL COMMENT '雪花ID',
    `order_no`       VARCHAR(32)     NOT NULL COMMENT '订单编号',
    `supply_id`      BIGINT                   COMMENT 'FK td_supply_info',
    `need_id`        BIGINT                   COMMENT 'FK td_purchase_need',
    `farmer_id`      BIGINT                   COMMENT 'FK uc_user (seller)',
    `buyer_id`       BIGINT                   COMMENT 'FK uc_user (buyer)',
    `variety`        VARCHAR(64)              COMMENT '品种',
    `quantity`       DECIMAL(12,2)            COMMENT '数量(kg)',
    `unit_price`     DECIMAL(10,2)            COMMENT '单价(元/kg)',
    `total_amount`   DECIMAL(14,2)            COMMENT '总金额(元)',
    `trade_date`     DATE                     COMMENT '交易日期',
    `delivery_date`  DATE                     COMMENT '发货日期',
    `payment_status` VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/REFUNDED',
    `order_status`   VARCHAR(16)     NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED/DELIVERED/COMPLETED/CANCELLED',
    `contract_file`  VARCHAR(512)             COMMENT 'MinIO合同文件路径',
    `remark`         VARCHAR(512),
    `create_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`      VARCHAR(64),
    `deleted`        TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_farmer_id` (`farmer_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易订单表';

CREATE TABLE IF NOT EXISTS `td_trade_payment` (
    `id`           BIGINT          NOT NULL COMMENT '雪花ID',
    `order_id`     BIGINT          NOT NULL COMMENT 'FK td_trade_order',
    `payment_no`   VARCHAR(64)     NOT NULL COMMENT '支付流水号',
    `payment_type` VARCHAR(16)              COMMENT 'ONLINE/OFFLINE',
    `amount`       DECIMAL(14,2)            COMMENT '金额',
    `pay_time`     DATETIME                 COMMENT '支付时间',
    `channel`      VARCHAR(64)              COMMENT '支付渠道',
    `voucher_file` VARCHAR(512)             COMMENT '凭证文件',
    `status`       VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/FAILED',
    `create_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`    VARCHAR(64),
    `deleted`      TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易支付表';

-- =============================================================================
-- SEED DATA
-- Password for all users: admin123
-- BCrypt hash of 'admin123': $2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6
-- =============================================================================

-- uc_user seed (10 users, all 8 role types)
INSERT IGNORE INTO `uc_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `role_code`, `status`, `org_name`, `org_type`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(1001, 'admin',     '$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '系统管理员', '13800000001', 'admin@apple-chain.com',     'ADMIN',      1, '苹果产业链平台',  '运营方',   NOW(), NOW(), 'system', 0),
(1002, 'farmer01',  '$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '张大农',   '13811111101', 'farmer01@example.com',     'FARMER',     1, '洛川张家果园',   '个体农户', NOW(), NOW(), 'system', 0),
(1003, 'farmer02',  '$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '李富贵',   '13811111102', 'farmer02@example.com',     'FARMER',     1, '延安李家苹果园', '个体农户', NOW(), NOW(), 'system', 0),
(1004, 'supplier01','$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '王供应',   '13822222201', 'supplier01@example.com',  'SUPPLIER',   1, '陕西苹果供应公司','供应商',   NOW(), NOW(), 'system', 0),
(1005, 'buyer01',   '$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '赵采购',   '13833333301', 'buyer01@example.com',     'BUYER',      1, '北京新发地批发市场','采购商', NOW(), NOW(), 'system', 0),
(1006, 'buyer02',   '$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '陈超市',   '13833333302', 'buyer02@example.com',     'BUYER',      1, '永辉超市陕西区', '采购商',   NOW(), NOW(), 'system', 0),
(1007, 'warehouse01','$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6','刘仓管',  '13844444401', 'warehouse01@example.com', 'WAREHOUSE',  1, '洛川冷链仓储中心','仓储方',  NOW(), NOW(), 'system', 0),
(1008, 'logistics01','$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6','孙物流',  '13855555501', 'logistics01@example.com', 'LOGISTICS',  1, '顺丰冷链物流',   '物流方',   NOW(), NOW(), 'system', 0),
(1009, 'finance01', '$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '周财务',   '13866666601', 'finance01@example.com',   'FINANCE',    1, '农业银行洛川支行','金融机构', NOW(), NOW(), 'system', 0),
(1010, 'gov01',     '$2a$10$X8BKGPwI2dY9VxlxL4XlAuPlwUwbN3Oo6LMHiZ3Wc/gvNAXIEMTf6', '郑监管',   '13877777701', 'gov01@example.com',       'GOV',        1, '洛川县农业农村局','政府机构', NOW(), NOW(), 'system', 0);

-- pt_orchard seed (5 orchards)
INSERT IGNORE INTO `pt_orchard` (`id`, `orchard_no`, `orchard_name`, `farmer_id`, `area`, `variety`, `location`, `longitude`, `latitude`, `tree_age`, `status`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(2001, 'ORD202501010001', '张家湾苹果园',    1002, 120.50, '红富士', '陕西省延安市洛川县旧县镇张家湾村', 109.4321, 35.7654, 15, 'NORMAL',    NOW(), NOW(), 'system', 0),
(2002, 'ORD202501010002', '李家沟有机果园',  1003, 85.20,  '嘎拉',   '陕西省延安市洛川县交口河镇李家沟',  109.5678, 35.8123, 12, 'NORMAL',    NOW(), NOW(), 'system', 0),
(2003, 'ORD202501010003', '金苹果示范园',    1002, 200.00, '黄元帅', '陕西省延安市洛川县凤栖镇金家塬',   109.3987, 35.6789, 20, 'NORMAL',    NOW(), NOW(), 'system', 0),
(2004, 'ORD202501010004', '红星家庭农场',    1003, 65.80,  '红富士', '陕西省延安市宜川县丹州镇红旗村',   110.1234, 36.0456, 8,  'DORMANT',   NOW(), NOW(), 'system', 0),
(2005, 'ORD202501010005', '绿野苹果基地',    1002, 150.30, '秦冠',   '陕西省延安市黄陵县店头镇绿野村',   109.2345, 35.5678, 18, 'HARVESTED', NOW(), NOW(), 'system', 0);

-- pt_growth_record seed (8 records covering all types)
INSERT IGNORE INTO `pt_growth_record` (`id`, `orchard_id`, `record_type`, `operate_date`, `operator`, `materials`, `weather`, `notes`, `recorded_by`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(3001, 2001, 'FERTILIZE',    '2025-03-15', '张大农', '[{"name":"有机复合肥","amount":"200","unit":"kg"},{"name":"硫酸钾","amount":"50","unit":"kg"}]', '晴', '春季基肥，均匀撒施', 1002, NOW(), NOW(), 'system', 0),
(3002, 2001, 'SPRAY',        '2025-04-10', '张大农', '[{"name":"多菌灵","amount":"0.5","unit":"kg"},{"name":"水","amount":"200","unit":"L"}]',         '阴', '防治腐烂病，全株喷施', 1002, NOW(), NOW(), 'system', 0),
(3003, 2002, 'IRRIGATE',     '2025-05-20', '李富贵', '[{"name":"滴灌水","amount":"5000","unit":"L"}]',                                                 '晴', '果实膨大期补水', 1003, NOW(), NOW(), 'system', 0),
(3004, 2003, 'PRUNE',        '2025-02-28', '张大农', '[]',                                                                                               '晴', '冬季修剪，去除枯枝病枝', 1002, NOW(), NOW(), 'system', 0),
(3005, 2001, 'PEST_CONTROL', '2025-06-15', '张大农', '[{"name":"吡虫啉","amount":"100","unit":"mL"},{"name":"水","amount":"100","unit":"L"}]',          '多云', '苹果蚜虫防治', 1002, NOW(), NOW(), 'system', 0),
(3006, 2002, 'FERTILIZE',    '2025-07-01', '李富贵', '[{"name":"叶面肥","amount":"2","unit":"kg"},{"name":"水","amount":"100","unit":"L"}]',            '晴', '果实发育期叶面追肥', 1003, NOW(), NOW(), 'system', 0),
(3007, 2004, 'SPRAY',        '2025-08-10', '李富贵', '[{"name":"波尔多液","amount":"5","unit":"kg"},{"name":"水","amount":"250","unit":"L"}]',          '阴', '预防轮纹病', 1003, NOW(), NOW(), 'system', 0),
(3008, 2003, 'IRRIGATE',     '2025-09-05', '张大农', '[{"name":"滴灌水","amount":"8000","unit":"L"}]',                                                 '晴', '采前补水，提高果实品质', 1002, NOW(), NOW(), 'system', 0);

-- pt_harvest_batch seed (5 batches with mixed statuses)
INSERT IGNORE INTO `pt_harvest_batch` (`id`, `batch_no`, `orchard_id`, `harvest_date`, `total_weight`, `grade_a`, `grade_b`, `grade_c`, `status`, `trace_code`, `remark`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(4001, 'HB202510010001', 2001, '2025-10-05', 45000.00, 30000.00, 10000.00, 5000.00,  'CONFIRMED',   'TCHB202510010001', '优质红富士，果形端正', NOW(), NOW(), 'system', 0),
(4002, 'HB202510010002', 2002, '2025-10-08', 28000.00, 18000.00, 7000.00,  3000.00,  'IN_STORAGE',  'TCHB202510010002', '嘎拉早熟品种，脆甜', NOW(), NOW(), 'system', 0),
(4003, 'HB202510010003', 2003, '2025-10-12', 62000.00, 40000.00, 15000.00, 7000.00,  'CONFIRMED',   'TCHB202510010003', '黄元帅大批次', NOW(), NOW(), 'system', 0),
(4004, 'HB202510010004', 2005, '2025-09-28', 38000.00, 22000.00, 12000.00, 4000.00,  'DRAFT',       NULL,               '秦冠，待确认', NOW(), NOW(), 'system', 0),
(4005, 'HB202510010005', 2001, '2025-10-20', 51000.00, 35000.00, 11000.00, 5000.00,  'DRAFT',       NULL,               '红富士二批次', NOW(), NOW(), 'system', 0);

-- tr_trace_chain seed (5 chains)
INSERT IGNORE INTO `tr_trace_chain` (`id`, `trace_code`, `product_type`, `batch_no`, `orchard_id`, `farmer_id`, `current_status`, `data_hash`, `chain_status`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(5001, 'TCHB202510010001', 'APPLE', 'HB202510010001', 2001, 1002, 'IN_TRANSIT', 'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6', 1, NOW(), NOW(), 'system', 0),
(5002, 'TCHB202510010002', 'APPLE', 'HB202510010002', 2002, 1003, 'IN_STORAGE', 'b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7', 1, NOW(), NOW(), 'system', 0),
(5003, 'TCHB202510010003', 'APPLE', 'HB202510010003', 2003, 1002, 'SOLD',       'c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8', 0, NOW(), NOW(), 'system', 0),
(5004, 'TCHB202510010004', 'APPLE', 'HB202510010002', 2002, 1003, 'HARVESTED',  'd4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9', 0, NOW(), NOW(), 'system', 0),
(5005, 'TCHB202510010005', 'APPLE', 'HB202510010001', 2001, 1002, 'PLANTED',    'e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0', 0, NOW(), NOW(), 'system', 0);

-- tr_trace_node seed (15 nodes, 3 per chain)
INSERT IGNORE INTO `tr_trace_node` (`id`, `trace_code`, `node_type`, `node_time`, `operator_id`, `operator_name`, `summary`, `detail`, `location`, `data_hash`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
-- Chain 5001
(6001, 'TCHB202510010001', 'PLANT',     '2020-03-01 08:00:00', 1002, '张大农', '完成建园定植，品种红富士', '{"variety":"红富士","count":800,"spacing":"4m*5m"}',   '陕西省延安市洛川县旧县镇', 'hash001', NOW(), NOW(), 'system', 0),
(6002, 'TCHB202510010001', 'HARVEST',   '2025-10-05 07:30:00', 1002, '张大农', '完成采收，总量45000kg',    '{"totalWeight":45000,"gradeA":30000,"workers":20}',    '陕西省延安市洛川县旧县镇', 'hash002', NOW(), NOW(), 'system', 0),
(6003, 'TCHB202510010001', 'LOGISTICS', '2025-10-07 10:00:00', 1008, '孙物流', '冷链运输发往北京',        '{"vehicle":"冷链车川A88888","temp":2,"dest":"北京新发地"}', '陕西省西安市高速公路',     'hash003', NOW(), NOW(), 'system', 0),
-- Chain 5002
(6004, 'TCHB202510010002', 'PLANT',     '2021-04-01 08:00:00', 1003, '李富贵', '嘎拉苗木定植完成',       '{"variety":"嘎拉","count":600}',                        '陕西省延安市洛川县',       'hash004', NOW(), NOW(), 'system', 0),
(6005, 'TCHB202510010002', 'HARVEST',   '2025-10-08 08:00:00', 1003, '李富贵', '采收完成，入库待检',      '{"totalWeight":28000,"gradeA":18000}',                  '陕西省延安市洛川县',       'hash005', NOW(), NOW(), 'system', 0),
(6006, 'TCHB202510010002', 'STORAGE',   '2025-10-09 14:00:00', 1007, '刘仓管', '入洛川冷链仓储，保鲜存储', '{"warehouseId":1007,"temp":-1,"humidity":95}',          '陕西省延安市洛川县冷链中心', 'hash006', NOW(), NOW(), 'system', 0),
-- Chain 5003
(6007, 'TCHB202510010003', 'PLANT',     '2019-03-15 09:00:00', 1002, '张大农', '黄元帅矮化砧木定植',      '{"variety":"黄元帅","rootstock":"矮化砧"}',              '陕西省延安市洛川县凤栖镇', 'hash007', NOW(), NOW(), 'system', 0),
(6008, 'TCHB202510010003', 'HARVEST',   '2025-10-12 06:30:00', 1002, '张大农', '大批次采收62000kg完成',   '{"totalWeight":62000,"workers":35}',                    '陕西省延安市洛川县凤栖镇', 'hash008', NOW(), NOW(), 'system', 0),
(6009, 'TCHB202510010003', 'TRADE',     '2025-10-15 10:00:00', 1005, '赵采购', '北京新发地成交',          '{"buyerId":1005,"price":4.8,"amount":297600}',           '北京市新发地农产品批发市场', 'hash009', NOW(), NOW(), 'system', 0),
-- Chain 5004
(6010, 'TCHB202510010004', 'PLANT',     '2021-04-01 08:00:00', 1003, '李富贵', '果园定植记录',            '{"variety":"嘎拉","area":85.2}',                        '陕西省延安市洛川县',       'hash010', NOW(), NOW(), 'system', 0),
(6011, 'TCHB202510010004', 'GROW',      '2025-06-20 09:00:00', 1003, '李富贵', '夏季修剪及病虫害防治',    '{"operations":["prune","spray"]}',                      '陕西省延安市洛川县',       'hash011', NOW(), NOW(), 'system', 0),
(6012, 'TCHB202510010004', 'HARVEST',   '2025-10-08 08:00:00', 1003, '李富贵', '采收完成',                '{"totalWeight":28000}',                                 '陕西省延安市洛川县',       'hash012', NOW(), NOW(), 'system', 0),
-- Chain 5005
(6013, 'TCHB202510010005', 'PLANT',     '2020-03-01 08:00:00', 1002, '张大农', '红富士建园',              '{"variety":"红富士","count":800}',                      '陕西省延安市洛川县旧县镇', 'hash013', NOW(), NOW(), 'system', 0),
(6014, 'TCHB202510010005', 'GROW',      '2025-08-15 09:00:00', 1002, '张大农', '套袋管理，提升果面光洁度', '{"bags":80000,"date":"2025-08-15"}',                    '陕西省延安市洛川县旧县镇', 'hash014', NOW(), NOW(), 'system', 0),
(6015, 'TCHB202510010005', 'HARVEST',   '2025-10-20 07:00:00', 1002, '张大农', '二批次采收51000kg',       '{"totalWeight":51000,"gradeA":35000,"workers":25}',     '陕西省延安市洛川县旧县镇', 'hash015', NOW(), NOW(), 'system', 0);

-- td_supply_info seed (5 supplies, mixed statuses)
INSERT IGNORE INTO `td_supply_info` (`id`, `supply_no`, `farmer_id`, `orchard_id`, `variety`, `quantity`, `price_expected`, `harvest_date`, `valid_until`, `quality`, `location`, `description`, `status`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(7001, 'SUP202510010001', 1002, 2001, '红富士', 20000.00, 4.50, '2025-10-05', '2025-11-30', 'A', '陕西省延安市洛川县', '优质红富士，套袋果，果径80mm以上', 'PUBLISHED', NOW(), NOW(), 'system', 0),
(7002, 'SUP202510010002', 1003, 2002, '嘎拉',   15000.00, 3.80, '2025-10-08', '2025-11-15', 'B', '陕西省延安市洛川县', '嘎拉早熟，甜脆可口',               'MATCHED',   NOW(), NOW(), 'system', 0),
(7003, 'SUP202510010003', 1002, 2003, '黄元帅', 30000.00, 3.20, '2025-10-12', '2025-12-31', 'A', '陕西省延安市洛川县', '黄元帅优质果，适合礼品装',          'PUBLISHED', NOW(), NOW(), 'system', 0),
(7004, 'SUP202510010004', 1003, 2004, '红富士', 8000.00,  4.20, '2025-10-15', '2025-12-01', 'C', '陕西省延安市宜川县', '富士散装果，适合加工',               'DRAFT',     NOW(), NOW(), 'system', 0),
(7005, 'SUP202510010005', 1002, 2005, '秦冠',   18000.00, 2.80, '2025-09-28', '2025-11-01', 'B', '陕西省延安市黄陵县', '秦冠早熟，价格实惠',                 'CLOSED',    NOW(), NOW(), 'system', 0);

-- td_purchase_need seed (5 needs, mixed statuses)
INSERT IGNORE INTO `td_purchase_need` (`id`, `need_no`, `buyer_id`, `variety`, `quantity`, `price_max`, `require_date`, `quality`, `delivery_addr`, `description`, `status`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(8001, 'NED202510010001', 1005, '红富士', 25000.00, 5.00, '2025-11-01', 'A', '北京市丰台区新发地市场', '需要套袋优质红富士，果径85mm以上',  'PUBLISHED', NOW(), NOW(), 'system', 0),
(8002, 'NED202510010002', 1006, '嘎拉',   10000.00, 4.20, '2025-10-20', 'B', '陕西省西安市雁塔区永辉超市仓库', '商超渠道嘎拉，需统一规格包装',    'MATCHED',   NOW(), NOW(), 'system', 0),
(8003, 'NED202510010003', 1005, '黄元帅', 20000.00, 3.80, '2025-11-15', 'A', '北京市新发地市场F区',   '黄元帅大量需求，长期合作优先',      'PUBLISHED', NOW(), NOW(), 'system', 0),
(8004, 'NED202510010004', 1006, '红富士', 5000.00,  4.60, '2025-10-25', 'A', '陕西省西安市永辉超市配送中心', '节日礼品盒用，外观要求高',        'DRAFT',     NOW(), NOW(), 'system', 0),
(8005, 'NED202510010005', 1005, '秦冠',   15000.00, 3.00, '2025-10-10', 'B', '北京市新发地市场',       '加工用秦冠，价格合理即可',          'CLOSED',    NOW(), NOW(), 'system', 0);

-- td_trade_order seed (5 orders, different statuses)
INSERT IGNORE INTO `td_trade_order` (`id`, `order_no`, `supply_id`, `need_id`, `farmer_id`, `buyer_id`, `variety`, `quantity`, `unit_price`, `total_amount`, `trade_date`, `delivery_date`, `payment_status`, `order_status`, `remark`, `create_time`, `update_time`, `create_by`, `deleted`) VALUES
(9001, 'ORD202510010001', 7001, 8001, 1002, 1005, '红富士', 15000.00, 4.60, 69000.00,  '2025-10-10', '2025-10-20', 'PAID',    'COMPLETED', '首批次合同交付完成', NOW(), NOW(), 'system', 0),
(9002, 'ORD202510010002', 7002, 8002, 1003, 1006, '嘎拉',   8000.00,  3.90, 31200.00,  '2025-10-11', '2025-10-18', 'PAID',    'DELIVERED', '已发货等待签收',    NOW(), NOW(), 'system', 0),
(9003, 'ORD202510010003', 7003, 8003, 1002, 1005, '黄元帅', 20000.00, 3.50, 70000.00,  '2025-10-13', '2025-10-25', 'PENDING', 'CONFIRMED', '已确认待付款发货',  NOW(), NOW(), 'system', 0),
(9004, 'ORD202510010004', 7001, 8001, 1002, 1005, '红富士', 5000.00,  4.70, 23500.00,  '2025-10-15', '2025-10-28', 'PENDING', 'DRAFT',     '等待确认',          NOW(), NOW(), 'system', 0),
(9005, 'ORD202510010005', 7003, 8003, 1002, 1006, '黄元帅', 10000.00, 3.40, 34000.00,  '2025-10-16', NULL,         'PENDING', 'CANCELLED', '买家取消订单',      NOW(), NOW(), 'system', 0);
