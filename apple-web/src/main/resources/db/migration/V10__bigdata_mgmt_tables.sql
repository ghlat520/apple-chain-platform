-- =============================================
-- V10: Big-data management backbone (10 modules)
-- M2 of the bigdata-platform rollout.
-- All tables use `bd_` prefix and inherit the
-- project convention: snowflake id, soft-delete,
-- create/update audit fields.
-- =============================================

-- ---------------------------------------------
-- 1. Data source registry
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_data_source (
    id              BIGINT        PRIMARY KEY,
    source_code     VARCHAR(64)   NOT NULL COMMENT '数据源编码（唯一）',
    source_name     VARCHAR(128)  NOT NULL COMMENT '数据源名称',
    source_type     VARCHAR(32)   NOT NULL COMMENT 'MYSQL/CLICKHOUSE/KAFKA/HTTP_API/RSS/FILE',
    category        VARCHAR(32)   NOT NULL COMMENT 'INTERNAL/EXTERNAL',
    connect_url     VARCHAR(512)  NULL     COMMENT 'jdbc url / http endpoint',
    username        VARCHAR(128)  NULL,
    password_enc    VARCHAR(512)  NULL     COMMENT 'AES 加密后的密码',
    extra_config    TEXT          NULL     COMMENT 'JSON 扩展配置',
    owner           VARCHAR(64)   NULL,
    status          VARCHAR(16)   DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED/ERROR',
    last_test_time  DATETIME      NULL,
    last_test_result VARCHAR(512) NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_source_code (source_code, deleted)
) COMMENT='大数据-数据源注册表';

-- ---------------------------------------------
-- 2. Collect job + run history
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_collect_job (
    id              BIGINT        PRIMARY KEY,
    job_code        VARCHAR(64)   NOT NULL,
    job_name        VARCHAR(128)  NOT NULL,
    source_id       BIGINT        NOT NULL COMMENT '关联 bd_data_source',
    job_type        VARCHAR(32)   NOT NULL COMMENT 'CDC/SCHEDULED/MANUAL/STREAMING',
    cron_expr       VARCHAR(64)   NULL     COMMENT 'Quartz cron',
    target_table    VARCHAR(128)  NULL     COMMENT '目标表（ClickHouse 库.表）',
    script          TEXT          NULL     COMMENT '执行脚本/SQL/Adapter class',
    enabled         TINYINT       DEFAULT 1,
    retry_times     INT           DEFAULT 3,
    timeout_seconds INT           DEFAULT 300,
    last_run_time   DATETIME      NULL,
    last_run_status VARCHAR(16)   NULL     COMMENT 'SUCCESS/FAILED/RUNNING',
    owner           VARCHAR(64)   NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_job_code (job_code, deleted),
    INDEX idx_source (source_id)
) COMMENT='大数据-采集任务定义';

CREATE TABLE IF NOT EXISTS bd_collect_job_run (
    id              BIGINT        PRIMARY KEY,
    job_id          BIGINT        NOT NULL,
    job_code        VARCHAR(64)   NOT NULL,
    run_status      VARCHAR(16)   NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED/TIMEOUT',
    start_time      DATETIME      NOT NULL,
    end_time        DATETIME      NULL,
    duration_ms     BIGINT        NULL,
    rows_read       BIGINT        DEFAULT 0,
    rows_written    BIGINT        DEFAULT 0,
    error_message   TEXT          NULL,
    trigger_type    VARCHAR(16)   DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/MANUAL/RETRY',
    triggered_by    VARCHAR(64)   NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_job (job_id, start_time),
    INDEX idx_status (run_status)
) COMMENT='大数据-采集任务运行记录';

-- ---------------------------------------------
-- 3. Data asset catalog (table + field metadata)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_data_asset (
    id              BIGINT        PRIMARY KEY,
    asset_code      VARCHAR(128)  NOT NULL COMMENT 'db.schema.table',
    database_name   VARCHAR(64)   NOT NULL,
    schema_name     VARCHAR(64)   NULL,
    table_name      VARCHAR(128)  NOT NULL,
    biz_name        VARCHAR(128)  NULL     COMMENT '业务名称',
    biz_description VARCHAR(1000) NULL     COMMENT '业务说明',
    owner           VARCHAR(64)   NULL     COMMENT '数据 Owner',
    security_level  VARCHAR(16)   DEFAULT 'INTERNAL' COMMENT 'PUBLIC/INTERNAL/SENSITIVE/SECRET',
    source_system   VARCHAR(64)   NULL     COMMENT '来源系统',
    record_count    BIGINT        DEFAULT 0,
    last_update_time DATETIME     NULL,
    tags            VARCHAR(500)  NULL     COMMENT '逗号分隔的标签',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_asset_code (asset_code, deleted)
) COMMENT='大数据-数据资产目录';

CREATE TABLE IF NOT EXISTS bd_data_asset_field (
    id              BIGINT        PRIMARY KEY,
    asset_id        BIGINT        NOT NULL,
    field_name      VARCHAR(128)  NOT NULL,
    field_type      VARCHAR(64)   NULL,
    biz_meaning     VARCHAR(500)  NULL,
    is_sensitive    TINYINT       DEFAULT 0 COMMENT '0=否 1=是',
    sensitive_type  VARCHAR(32)   NULL     COMMENT 'PHONE/ID_CARD/GPS/NAME/EMAIL',
    is_pk           TINYINT       DEFAULT 0,
    nullable        TINYINT       DEFAULT 1,
    default_value   VARCHAR(255)  NULL,
    ordinal         INT           DEFAULT 0,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_asset (asset_id)
) COMMENT='大数据-资产字段元数据';

-- ---------------------------------------------
-- 4. Metric definition + value snapshot
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_metric_definition (
    id              BIGINT        PRIMARY KEY,
    metric_code     VARCHAR(64)   NOT NULL,
    metric_name     VARCHAR(128)  NOT NULL,
    category        VARCHAR(32)   NOT NULL COMMENT 'PLANTING/TRADE/WAREHOUSE/LOGISTICS/FINANCE/TRACE',
    unit            VARCHAR(32)   NULL     COMMENT 'kg/元/%/亩/批',
    calc_formula    TEXT          NULL     COMMENT '口径描述',
    sql_expression  TEXT          NULL     COMMENT 'ClickHouse 查询 SQL',
    data_source_id  BIGINT        NULL,
    refresh_cron    VARCHAR(64)   NULL,
    owner           VARCHAR(64)   NULL,
    is_core         TINYINT       DEFAULT 0 COMMENT '是否核心指标',
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_metric_code (metric_code, deleted)
) COMMENT='大数据-指标定义';

CREATE TABLE IF NOT EXISTS bd_metric_value (
    id              BIGINT        PRIMARY KEY,
    metric_code     VARCHAR(64)   NOT NULL,
    stat_date       DATE          NOT NULL,
    stat_period     VARCHAR(16)   DEFAULT 'DAY' COMMENT 'DAY/WEEK/MONTH/YEAR',
    metric_value    DECIMAL(18,4) NOT NULL,
    dim_json        VARCHAR(1000) NULL     COMMENT '维度 JSON (品种/地区/…)',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_metric_date (metric_code, stat_date)
) COMMENT='大数据-指标取值快照';

-- ---------------------------------------------
-- 5. Data quality rules + check results
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_dq_rule (
    id              BIGINT        PRIMARY KEY,
    rule_code       VARCHAR(64)   NOT NULL,
    rule_name       VARCHAR(128)  NOT NULL,
    asset_id        BIGINT        NULL,
    asset_code      VARCHAR(128)  NULL,
    field_name      VARCHAR(128)  NULL,
    rule_type       VARCHAR(32)   NOT NULL COMMENT 'NOT_NULL/UNIQUE/RANGE/REGEX/ENUM/FRESHNESS',
    rule_expr       TEXT          NULL     COMMENT '规则表达式或 SQL WHERE 片段',
    severity        VARCHAR(16)   DEFAULT 'WARN' COMMENT 'BLOCK/ERROR/WARN/INFO',
    enabled         TINYINT       DEFAULT 1,
    schedule_cron   VARCHAR(64)   NULL,
    owner           VARCHAR(64)   NULL,
    last_run_time   DATETIME      NULL,
    last_score      DECIMAL(5,2)  NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_rule_code (rule_code, deleted)
) COMMENT='大数据-数据质量规则';

CREATE TABLE IF NOT EXISTS bd_dq_check_result (
    id              BIGINT        PRIMARY KEY,
    rule_id         BIGINT        NOT NULL,
    rule_code       VARCHAR(64)   NOT NULL,
    check_time      DATETIME      NOT NULL,
    total_rows      BIGINT        DEFAULT 0,
    bad_rows        BIGINT        DEFAULT 0,
    score           DECIMAL(5,2)  DEFAULT 100.00,
    pass            TINYINT       DEFAULT 1,
    sample_bad_rows TEXT          NULL     COMMENT 'JSON 异常样本（前 20 条）',
    error_message   TEXT          NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_rule_time (rule_id, check_time)
) COMMENT='大数据-数据质量检查结果';

-- ---------------------------------------------
-- 6. Data lineage (upstream -> downstream DAG)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_data_lineage (
    id              BIGINT        PRIMARY KEY,
    upstream_type   VARCHAR(32)   NOT NULL COMMENT 'TABLE/FIELD/JOB/METRIC/DASHBOARD/SYSTEM',
    upstream_id     VARCHAR(256)  NOT NULL COMMENT '上游标识（asset_code/metric_code 等）',
    upstream_name   VARCHAR(256)  NULL,
    downstream_type VARCHAR(32)   NOT NULL,
    downstream_id   VARCHAR(256)  NOT NULL,
    downstream_name VARCHAR(256)  NULL,
    relation_type   VARCHAR(32)   NOT NULL COMMENT 'DERIVES_FROM/TRANSFORMS/AGGREGATES/REFERENCES',
    pipeline_job_id BIGINT        NULL     COMMENT '关联的 collect_job / spark_sql',
    description     VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_upstream (upstream_type, upstream_id),
    INDEX idx_downstream (downstream_type, downstream_id)
) COMMENT='大数据-数据血缘关系';

-- ---------------------------------------------
-- 7. Dashboard config (screen + widgets)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_dashboard_config (
    id              BIGINT        PRIMARY KEY,
    dashboard_code  VARCHAR(64)   NOT NULL,
    dashboard_name  VARCHAR(128)  NOT NULL,
    category        VARCHAR(32)   NOT NULL COMMENT 'GOV/OPS/INDUSTRY/CUSTOM',
    layout_json     TEXT          NULL     COMMENT 'vue-grid-layout JSON',
    theme           VARCHAR(32)   DEFAULT 'dark',
    visibility      VARCHAR(32)   DEFAULT 'PRIVATE' COMMENT 'PUBLIC/PRIVATE/ROLE',
    visible_roles   VARCHAR(500)  NULL     COMMENT '逗号分隔角色码',
    published       TINYINT       DEFAULT 0,
    owner           VARCHAR(64)   NULL,
    refresh_seconds INT           DEFAULT 30,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_dashboard_code (dashboard_code, deleted)
) COMMENT='大数据-大屏配置';

CREATE TABLE IF NOT EXISTS bd_dashboard_widget (
    id              BIGINT        PRIMARY KEY,
    dashboard_id    BIGINT        NOT NULL,
    widget_code     VARCHAR(64)   NOT NULL,
    widget_name     VARCHAR(128)  NOT NULL,
    widget_type     VARCHAR(32)   NOT NULL COMMENT 'KPI/LINE/BAR/PIE/TABLE/MAP/TRACE/TEXT',
    data_source     VARCHAR(32)   NOT NULL COMMENT 'METRIC/SQL/API',
    data_ref        VARCHAR(256)  NOT NULL COMMENT 'metric_code / sql 名 / api 路径',
    position_json   VARCHAR(500)  NULL     COMMENT '{x,y,w,h}',
    option_json     TEXT          NULL     COMMENT 'ECharts option',
    ordinal         INT           DEFAULT 0,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_dashboard (dashboard_id)
) COMMENT='大数据-大屏 Widget';

-- ---------------------------------------------
-- 8. (Audit log - reuse existing audit_log table if present)
--    Only create if missing.
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_audit_log (
    id              BIGINT        PRIMARY KEY,
    user_id         BIGINT        NULL,
    username        VARCHAR(64)   NULL,
    role_code       VARCHAR(32)   NULL,
    module          VARCHAR(64)   NULL,
    action          VARCHAR(64)   NOT NULL,
    target_type     VARCHAR(64)   NULL,
    target_id       VARCHAR(128)  NULL,
    request_ip      VARCHAR(64)   NULL,
    request_uri     VARCHAR(256)  NULL,
    request_method  VARCHAR(16)   NULL,
    request_params  TEXT          NULL,
    response_status INT           NULL,
    duration_ms     BIGINT        NULL,
    error_message   VARCHAR(1000) NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_action (action)
) COMMENT='大数据-管理操作审计日志';

-- ---------------------------------------------
-- 9. Role / Permission matrix
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_role (
    id              BIGINT        PRIMARY KEY,
    role_code       VARCHAR(32)   NOT NULL,
    role_name       VARCHAR(64)   NOT NULL,
    description     VARCHAR(500)  NULL,
    sort_order      INT           DEFAULT 0,
    enabled         TINYINT       DEFAULT 1,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code, deleted)
) COMMENT='大数据-角色字典';

CREATE TABLE IF NOT EXISTS bd_permission (
    id              BIGINT        PRIMARY KEY,
    perm_code       VARCHAR(64)   NOT NULL,
    perm_name       VARCHAR(128)  NOT NULL,
    module          VARCHAR(32)   NOT NULL COMMENT 'BIGDATA/USER/TRADE/…',
    perm_type       VARCHAR(16)   NOT NULL COMMENT 'MENU/BUTTON/API/DATA',
    resource_pattern VARCHAR(256) NULL     COMMENT '菜单路径 / URI Ant pattern',
    parent_code     VARCHAR(64)   NULL,
    sort_order      INT           DEFAULT 0,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_perm_code (perm_code, deleted)
) COMMENT='大数据-权限条目';

CREATE TABLE IF NOT EXISTS bd_role_permission (
    id              BIGINT        PRIMARY KEY,
    role_code       VARCHAR(32)   NOT NULL,
    perm_code       VARCHAR(64)   NOT NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_role (role_code),
    INDEX idx_perm (perm_code)
) COMMENT='大数据-角色权限映射';

-- ---------------------------------------------
-- 10. API gateway (M5 · Open API)
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS bd_api_client (
    id              BIGINT        PRIMARY KEY,
    client_name     VARCHAR(128)  NOT NULL,
    app_key         VARCHAR(64)   NOT NULL,
    app_secret_enc  VARCHAR(256)  NOT NULL,
    owner           VARCHAR(64)   NULL,
    rate_limit_qps  INT           DEFAULT 10,
    daily_quota     INT           DEFAULT 10000,
    status          VARCHAR(16)   DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED/REVOKED',
    valid_until     DATE          NULL,
    remark          VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    UNIQUE KEY uk_app_key (app_key, deleted)
) COMMENT='大数据-开放 API 客户端';

CREATE TABLE IF NOT EXISTS bd_api_call_log (
    id              BIGINT        PRIMARY KEY,
    app_key         VARCHAR(64)   NULL,
    client_ip       VARCHAR(64)   NULL,
    api_path        VARCHAR(256)  NOT NULL,
    method          VARCHAR(16)   NOT NULL,
    status_code     INT           NULL,
    duration_ms     BIGINT        NULL,
    request_bytes   BIGINT        NULL,
    response_bytes  BIGINT        NULL,
    error_message   VARCHAR(500)  NULL,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)   NULL,
    deleted         INT           DEFAULT 0,
    INDEX idx_app_time (app_key, create_time),
    INDEX idx_path (api_path)
) COMMENT='大数据-开放 API 调用日志';

-- ---------------------------------------------
-- Seed data: register 8 internal sources + 4 external sources
-- ---------------------------------------------
INSERT INTO bd_data_source (id, source_code, source_name, source_type, category, connect_url, status, remark)
VALUES
(1001, 'SRC_PLANTING',  '种植生产库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '苹果种植生产模块'),
(1002, 'SRC_INPUT',     '农资管理库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '农资投入品模块'),
(1003, 'SRC_TRACE',     '全流程溯源库', 'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '奥链溯源模块'),
(1004, 'SRC_TRADE',     '收购交易库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '收购交易撮合模块'),
(1005, 'SRC_WAREHOUSE', '仓储管理库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '仓储与智能仓单模块'),
(1006, 'SRC_COLDCHAIN', '冷链物流库',  'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '冷链调度追踪模块'),
(1007, 'SRC_FINANCE',   '供应链金融库', 'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '供应链金融模块'),
(1008, 'SRC_USER',      '用户与运营库', 'MYSQL', 'INTERNAL', 'jdbc:mysql://localhost:3306/apple_chain', 'ACTIVE', '用户/权限/审计'),
(2001, 'EXT_WEATHER',   '和风天气 API','HTTP_API','EXTERNAL','https://devapi.qweather.com/v7', 'ACTIVE', '主产区气象数据'),
(2002, 'EXT_MARKET',    '新发地批发价','HTTP_API','EXTERNAL','http://www.xinfadi.com.cn/api',  'ACTIVE', '农产品批发价日更新'),
(2003, 'EXT_POLICY',    '农业部 RSS', 'RSS',    'EXTERNAL', 'http://www.moa.gov.cn/rss',       'ACTIVE', '政策法规订阅'),
(2004, 'EXT_CONSUMER',  '电商消费趋势','HTTP_API','EXTERNAL','https://open.taobao.com/trend',   'ACTIVE', '电商搜索/消费趋势');

-- Seed: 9 roles
INSERT INTO bd_role (id, role_code, role_name, sort_order, description)
VALUES
(3001, 'ADMIN',     '系统管理员',    1,  '平台最高权限'),
(3002, 'OPS',       '运营管理员',    2,  '业务运营 + 数据看板'),
(3003, 'FARMER',    '种植户',       10, '果园/作业/采收'),
(3004, 'SUPPLIER',  '农资供应商',    11, '农资商品与订单'),
(3005, 'BUYER',     '收购商',       12, '收购需求与下单'),
(3006, 'WAREHOUSE', '仓储服务商',    13, '仓库与仓单'),
(3007, 'LOGISTICS', '物流商',       14, '车辆/运单/温控'),
(3008, 'FINANCE',   '金融机构',     15, '融资审批/风控'),
(3009, 'GOV',       '政府监管',     20, '只读数据/溯源/告警');

-- Seed: 10 core metrics from existing DashboardMapper.xml
INSERT INTO bd_metric_definition (id, metric_code, metric_name, category, unit, calc_formula, is_core, owner)
VALUES
(4001, 'M_TOTAL_ORCHARDS',   '果园总数',        'PLANTING', '个', 'COUNT(pt_orchard WHERE deleted=0)', 1, 'bigdata'),
(4002, 'M_TOTAL_AREA',       '种植总面积',       'PLANTING', '亩', 'SUM(pt_orchard.area)', 1, 'bigdata'),
(4003, 'M_HARVEST_YEAR',     '本年采收总量',     'PLANTING', 'kg', 'SUM(pt_harvest_batch.weight_kg WHERE YEAR=current)', 1, 'bigdata'),
(4004, 'M_TRADE_COUNT',      '成交订单数',       'TRADE',    '笔', 'COUNT(td_trade_order WHERE status=COMPLETED)', 1, 'bigdata'),
(4005, 'M_TRADE_AMOUNT',     '成交总金额',       'TRADE',    '元', 'SUM(td_trade_order.total_amount)', 1, 'bigdata'),
(4006, 'M_HARVEST_TREND',    '近6月采收趋势',    'PLANTING', 'kg', '按月分组求和', 1, 'bigdata'),
(4007, 'M_TRADE_TREND',      '近6月交易趋势',    'TRADE',    '元', '按月分组求和', 1, 'bigdata'),
(4008, 'M_VARIETY_DIST',     '品种分布',        'PLANTING', 'kg', '按品种分组', 1, 'bigdata'),
(4009, 'M_TOP_ORCHARDS',     'TOP5果园',       'PLANTING', 'kg', 'ORDER BY harvest DESC LIMIT 5', 1, 'bigdata'),
(4010, 'M_TRACE_STATUS',     '溯源状态分布',     'TRACE',    '条', '按状态分组', 1, 'bigdata');
