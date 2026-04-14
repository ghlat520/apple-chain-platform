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
