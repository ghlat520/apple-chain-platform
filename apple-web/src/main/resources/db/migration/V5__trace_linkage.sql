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
