-- =====================================================================
-- V31 — pt_growth_record 新增 photo_urls（结构化照片列表）
-- =====================================================================
-- 之前 H5 用 notes 字符串拼 [photos]N 做临时计数，现改为存储 JSON 数组字符串：
--   ["/api/files/ab12...", "/api/files/cd34..."]
-- 由 apple-module-file 上传接口产生。
-- =====================================================================

ALTER TABLE pt_growth_record
    ADD COLUMN photo_urls VARCHAR(2000) NULL COMMENT '照片 URL JSON 数组字符串' AFTER notes;
