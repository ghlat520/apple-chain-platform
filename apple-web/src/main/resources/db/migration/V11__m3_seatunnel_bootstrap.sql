-- =============================================
-- V11: M3 SeaTunnel + DataHub bootstrap
-- D1 决策: SeaTunnel 替代 Canal + 4 Java Adapter
-- D2 决策: DataHub OpenLineage (M4 部署, M3 预留 external_urn)
-- 详见 docs/pipeline/apple-chain-platform/M3-readiness-pack.md
-- =============================================

-- ---------------------------------------------
-- 1. bd_data_lineage 增加 DataHub URN 预留字段
--    M4 DataHub 部署后写入 urn:li:dataset:(...)
-- ---------------------------------------------
ALTER TABLE bd_data_lineage
    ADD COLUMN external_urn VARCHAR(512) NULL COMMENT 'DataHub URN 预留 (M4 启用)' AFTER description,
    ADD INDEX idx_external_urn (external_urn);

-- ---------------------------------------------
-- 2. bd_data_source: 12 条种子 (8 内部 + 4 外部)
--    source_type 值域扩展: SEATUNNEL_CDC / SEATUNNEL_HTTP
-- ---------------------------------------------
INSERT INTO bd_data_source
(id, source_code, source_name, source_type, category, connect_url, username, extra_config, owner, status, remark, create_by)
VALUES
-- 8 内部 (SeaTunnel MySQL-CDC, 同一物理库不同表)
(1101, 'INTERNAL_ORCHARD',       '果园基地业务库',     'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_orchard"]}',          'bigdata', 'ACTIVE', 'F-101 果园档案', 'system'),
(1102, 'INTERNAL_BATCH',         '生产批次库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_batch"]}',            'bigdata', 'ACTIVE', 'F-201 批次管理', 'system'),
(1103, 'INTERNAL_TRACE',         '溯源记录库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_traceability"]}',     'bigdata', 'ACTIVE', 'F-301 全链路溯源', 'system'),
(1104, 'INTERNAL_LOAN',          '产业链贷款库',       'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_loan"]}',     'bigdata', 'ACTIVE', 'F-401 金融贷款', 'system'),
(1105, 'INTERNAL_INSURANCE',     '农业保险库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_insurance"]}','bigdata','ACTIVE', 'F-402 农业保险', 'system'),
(1106, 'INTERNAL_FUTURES',       '期货期权库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_futures"]}',  'bigdata', 'ACTIVE', 'F-403 期货期权', 'system'),
(1107, 'INTERNAL_DEPOSIT',       '冷库订金库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_finance_deposit"]}',  'bigdata', 'ACTIVE', 'F-404 冷库订金', 'system'),
(1108, 'INTERNAL_IOT_DEVICE',    'IoT 设备库',         'SEATUNNEL_CDC',  'INTERNAL', 'jdbc:mysql://host.docker.internal:3306/apple_chain', 'canal', '{"job":"internal-apple-chain-cdc","tables":["t_iot_device"]}',       'bigdata', 'ACTIVE', 'F-501 5G+北斗 IoT', 'system'),
-- 4 外部 (SeaTunnel Http Source)
(1201, 'EXT_WEATHER_HEFENG',     '和风天气',           'SEATUNNEL_HTTP', 'EXTERNAL', 'https://devapi.qweather.com/v7/weather/24h',         NULL,    '{"job":"ext-weather-hefeng","auth_apollo_key":"bigdata.weather.hefeng.apikey"}', 'bigdata', 'ACTIVE', 'F-801 外部气象', 'system'),
(1202, 'EXT_PRICE_XINFADI',      '北京新发地批发价',   'SEATUNNEL_HTTP', 'EXTERNAL', 'http://www.xinfadi.com.cn/getPriceData.html',        NULL,    '{"job":"ext-market-price-xinfadi","auth":"none"}',                              'bigdata', 'ACTIVE', 'F-801 行情参考', 'system'),
(1203, 'EXT_POLICY_MOA_NDRC',    '农业部+发改委 RSS',  'SEATUNNEL_HTTP', 'EXTERNAL', 'http://www.moa.gov.cn/govpublic/rss.xml',            NULL,    '{"job":"ext-policy-moa-ndrc","auth":"none","compliance":"F-805"}',              'bigdata', 'ACTIVE', 'F-805 监管溯源', 'system'),
(1204, 'EXT_CONSUMER_JD',        '京东商智搜索热度',   'SEATUNNEL_HTTP', 'EXTERNAL', 'https://api.jd.com/routerjson',                      NULL,    '{"job":"ext-consumer-trend","auth_apollo_key":"bigdata.jd.appkey"}',            'bigdata', 'ACTIVE', 'F-801 消费趋势', 'system');

-- ---------------------------------------------
-- 3. bd_collect_job: 5 条作业登记
-- ---------------------------------------------
INSERT INTO bd_collect_job
(id, job_code, job_name, source_id, job_type, cron_expr, target_table, script, enabled, retry_times, timeout_seconds, create_by)
VALUES
(2001, 'INTERNAL_APPLE_CHAIN_CDC', '内部业务库 CDC',        1101, 'STREAMING', NULL,           'cdc.apple_chain.*',                           '/opt/seatunnel/jobs/internal/internal-apple-chain-cdc.conf',    1, 3,  0,    'system'),
(2101, 'EXT_WEATHER_HEFENG',       '和风天气 5min 采集',    1201, 'SCHEDULED', '0 */5 * * * ?', 'apple_bigdata_ext.weather_hourly',            '/opt/seatunnel/jobs/external/ext-weather-hefeng.conf',          1, 3,  300,  'system'),
(2102, 'EXT_PRICE_XINFADI',        '新发地批发价日采集',    1202, 'SCHEDULED', '0 30 2 * * ?',  'apple_bigdata_ext.market_price_daily',        '/opt/seatunnel/jobs/external/ext-market-price-xinfadi.conf',    1, 3,  600,  'system'),
(2103, 'EXT_POLICY_MOA_NDRC',      '部委政策 RSS 日采集',   1203, 'SCHEDULED', '0 0 6 * * ?',   'apple_bigdata_ext.policy_article',            '/opt/seatunnel/jobs/external/ext-policy-moa-ndrc.conf',         1, 3,  600,  'system'),
(2104, 'EXT_CONSUMER_TREND',       '电商搜索热度日采集',    1204, 'SCHEDULED', '0 0 3 * * ?',   'apple_bigdata_ext.consumer_trend',            '/opt/seatunnel/jobs/external/ext-consumer-trend.conf',          1, 3,  600,  'system');
