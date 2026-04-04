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
