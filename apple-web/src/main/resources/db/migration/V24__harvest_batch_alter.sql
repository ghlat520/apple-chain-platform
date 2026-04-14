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
