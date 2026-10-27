-- V26: Anomaly trace table for trace module
CREATE TABLE IF NOT EXISTS tr_anomaly_trace (
    id                      BIGINT          NOT NULL    PRIMARY KEY,
    trace_code              VARCHAR(64)     NULL,
    batch_id                BIGINT          NULL,
    anomaly_type            VARCHAR(32)     NULL        COMMENT 'PESTICIDE_EXCESS/TEMP_VIOLATION/QUALITY_FAIL/OTHER',
    description             VARCHAR(512)    NULL,
    severity                VARCHAR(16)     NULL        COMMENT 'HIGH/MEDIUM/LOW',
    status                  VARCHAR(16)     NOT NULL    DEFAULT 'OPEN'  COMMENT 'OPEN/INVESTIGATING/RESOLVED',
    affected_batch_count    INT             NULL,
    affected_fruit_count    INT             NULL,
    root_cause_analysis     VARCHAR(1024)   NULL,
    resolved_by             VARCHAR(64)     NULL,
    resolved_time           DATETIME        NULL,
    remark                  VARCHAR(512)    NULL,
    create_time             DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME        NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by               VARCHAR(64)     NULL,
    deleted                 TINYINT         NOT NULL    DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常追溯表';
