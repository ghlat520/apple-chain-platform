-- V25: Quality inspection table for trade module
CREATE TABLE IF NOT EXISTS td_quality_inspection (
    id              BIGINT          NOT NULL    PRIMARY KEY,
    inspection_no   VARCHAR(32)     NULL,
    order_id        BIGINT          NULL,
    supply_id       BIGINT          NULL,
    variety         VARCHAR(64)     NULL,
    grade           VARCHAR(4)      NULL,
    brix_value      DECIMAL(6,2)    NULL        COMMENT '糖度 Brix',
    firmness_value  DECIMAL(6,2)    NULL        COMMENT '硬度',
    color_score     DECIMAL(6,2)    NULL        COMMENT '色泽评分',
    defect_rate     DECIMAL(6,2)    NULL        COMMENT '缺陷率 %',
    inspector_name  VARCHAR(64)     NULL,
    inspection_date DATE            NULL,
    report_url      VARCHAR(512)    NULL,
    result          VARCHAR(16)     NULL        COMMENT 'PASS/FAIL/CONDITIONAL',
    status          VARCHAR(16)     NOT NULL    DEFAULT 'PENDING'  COMMENT 'PENDING/INSPECTED/ACCEPTED/DISPUTED',
    remark          VARCHAR(512)    NULL,
    create_time     DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(64)     NULL,
    deleted         TINYINT         NOT NULL    DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质量检验表';
