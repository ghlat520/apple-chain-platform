-- =====================================================================
-- V30 — 文件上传记录表
-- =====================================================================
-- 本迁移仅建元数据表。文件物理存储：
--   - 本地：apple.file.storage.local.root (默认 /tmp/apple-chain-files)
--   - OSS ：预留接口 OssFileStorageService（未实现）
-- 对外 URL：/api/files/{file_id}
-- =====================================================================

CREATE TABLE IF NOT EXISTS file_record (
    id              BIGINT        NOT NULL PRIMARY KEY COMMENT '雪花 ID',
    file_id         VARCHAR(64)   NOT NULL COMMENT '对外 fileId（UUID，无扩展名）',
    filename        VARCHAR(255)  NOT NULL COMMENT '物理文件名（{fileId}.{ext}）',
    original_name   VARCHAR(255)  NULL     COMMENT '上传时原始文件名（已清洗）',
    storage_path    VARCHAR(512)  NOT NULL COMMENT '存储路径（本地相对路径 / OSS object key）',
    url             VARCHAR(512)  NOT NULL COMMENT '公网访问路径（以 /api/files/ 开头）',
    size            BIGINT        NOT NULL COMMENT '字节数',
    content_type    VARCHAR(64)   NOT NULL COMMENT 'MIME 类型',
    biz_type        VARCHAR(32)   NULL     COMMENT '业务类型（如 growth-record）',
    biz_id          BIGINT        NULL     COMMENT '业务实体 ID（可选，挂接点）',
    uploader_id     BIGINT        NULL     COMMENT '上传者 uc_user.id',
    upload_time     DATETIME      NOT NULL COMMENT '上传时间',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(64)   NULL,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    UNIQUE KEY uk_file_record_file_id (file_id),
    KEY idx_file_record_biz (biz_type, biz_id),
    KEY idx_file_record_uploader (uploader_id),
    KEY idx_file_record_upload_time (upload_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传记录';
