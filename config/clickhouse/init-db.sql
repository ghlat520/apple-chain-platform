-- =============================================
-- ClickHouse initialization for apple-chain-platform bigdata stack
-- Runs once on first container start via docker-entrypoint-initdb.d
-- =============================================

-- Three-tier warehouse: ODS (raw) / DWD (cleansed) / DWS (aggregated)
CREATE DATABASE IF NOT EXISTS apple_bigdata_ods;
CREATE DATABASE IF NOT EXISTS apple_bigdata_dwd;
CREATE DATABASE IF NOT EXISTS apple_bigdata_dws;

-- External data sources (weather / market / policy / consumer)
CREATE DATABASE IF NOT EXISTS apple_bigdata_ext;

-- ---------------------------------------------
-- ODS layer: mirrors business tables via Canal CDC
-- Keep schema flexible; use ReplacingMergeTree by primary key.
-- ---------------------------------------------

CREATE TABLE IF NOT EXISTS apple_bigdata_ods.pt_orchard
(
    id           Int64,
    name         String,
    area         Decimal(12, 2),
    owner_id     Int64,
    variety      String,
    province     String,
    city         String,
    create_time  DateTime,
    update_time  DateTime,
    deleted      Int8 DEFAULT 0,
    _cdc_op      LowCardinality(String) DEFAULT 'INSERT',
    _cdc_ts      DateTime DEFAULT now()
)
ENGINE = ReplacingMergeTree(_cdc_ts)
ORDER BY (id)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS apple_bigdata_ods.pt_harvest_batch
(
    id             Int64,
    orchard_id     Int64,
    variety        String,
    weight_kg      Decimal(14, 2),
    quality_grade  String,
    harvest_date   Date,
    create_time    DateTime,
    update_time    DateTime,
    deleted        Int8 DEFAULT 0,
    _cdc_op        LowCardinality(String) DEFAULT 'INSERT',
    _cdc_ts        DateTime DEFAULT now()
)
ENGINE = ReplacingMergeTree(_cdc_ts)
ORDER BY (id)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS apple_bigdata_ods.td_trade_order
(
    id              Int64,
    order_no        String,
    buyer_id        Int64,
    seller_id       Int64,
    variety         String,
    quantity_kg     Decimal(14, 2),
    unit_price      Decimal(10, 2),
    total_amount    Decimal(14, 2),
    status          LowCardinality(String),
    create_time     DateTime,
    update_time     DateTime,
    deleted         Int8 DEFAULT 0,
    _cdc_op         LowCardinality(String) DEFAULT 'INSERT',
    _cdc_ts         DateTime DEFAULT now()
)
ENGINE = ReplacingMergeTree(_cdc_ts)
ORDER BY (id)
SETTINGS index_granularity = 8192;

-- ---------------------------------------------
-- DWS layer: pre-aggregated metrics for dashboards
-- ---------------------------------------------

CREATE TABLE IF NOT EXISTS apple_bigdata_dws.dws_daily_kpi
(
    stat_date        Date,
    total_orchards   UInt64,
    total_area       Decimal(14, 2),
    total_harvest_kg Decimal(14, 2),
    total_trades     UInt64,
    total_amount     Decimal(16, 2),
    active_farmers   UInt64,
    optimal_rate     Decimal(5, 2)
)
ENGINE = ReplacingMergeTree()
ORDER BY (stat_date)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS apple_bigdata_dws.dws_monthly_trend
(
    stat_month       String,
    metric_code      LowCardinality(String),
    metric_value     Decimal(18, 4),
    update_time      DateTime DEFAULT now()
)
ENGINE = ReplacingMergeTree(update_time)
ORDER BY (stat_month, metric_code)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS apple_bigdata_dws.dws_variety_distribution
(
    variety          String,
    total_weight_kg  Decimal(16, 2),
    total_amount     Decimal(16, 2),
    update_time      DateTime DEFAULT now()
)
ENGINE = ReplacingMergeTree(update_time)
ORDER BY (variety)
SETTINGS index_granularity = 8192;

-- ---------------------------------------------
-- EXT layer: external data sources
-- ---------------------------------------------

CREATE TABLE IF NOT EXISTS apple_bigdata_ext.weather_hourly
(
    city          String,
    obs_time      DateTime,
    temperature   Decimal(5, 2),
    humidity      Decimal(5, 2),
    wind_speed    Decimal(5, 2),
    condition     String,
    source        LowCardinality(String) DEFAULT 'qweather',
    ingest_time   DateTime DEFAULT now()
)
ENGINE = MergeTree()
ORDER BY (city, obs_time)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS apple_bigdata_ext.market_price_daily
(
    market_name   String,
    product_name  String,
    variety       String,
    price_min     Decimal(10, 2),
    price_avg     Decimal(10, 2),
    price_max     Decimal(10, 2),
    unit          String,
    price_date    Date,
    source        LowCardinality(String) DEFAULT 'xinfadi',
    ingest_time   DateTime DEFAULT now()
)
ENGINE = MergeTree()
ORDER BY (price_date, market_name, product_name)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS apple_bigdata_ext.policy_article
(
    id            String,
    title         String,
    source        String,
    pub_date      DateTime,
    url           String,
    summary       String,
    category      LowCardinality(String),
    ingest_time   DateTime DEFAULT now()
)
ENGINE = MergeTree()
ORDER BY (pub_date)
SETTINGS index_granularity = 8192;

CREATE TABLE IF NOT EXISTS apple_bigdata_ext.consumer_trend
(
    platform      LowCardinality(String),
    keyword       String,
    search_volume UInt64,
    click_rate    Decimal(5, 2),
    stat_date     Date,
    ingest_time   DateTime DEFAULT now()
)
ENGINE = MergeTree()
ORDER BY (stat_date, platform, keyword)
SETTINGS index_granularity = 8192;

-- =============================================
-- M3 append: TTL policy for ext layer
-- (D1 SeaTunnel collection — see M3-readiness-pack.md §5)
-- =============================================
ALTER TABLE apple_bigdata_ext.weather_hourly
    MODIFY TTL ingest_time + INTERVAL 90 DAY;

ALTER TABLE apple_bigdata_ext.market_price_daily
    MODIFY TTL toDateTime(price_date) + INTERVAL 730 DAY;

ALTER TABLE apple_bigdata_ext.policy_article
    MODIFY TTL pub_date + INTERVAL 1825 DAY;

ALTER TABLE apple_bigdata_ext.consumer_trend
    MODIFY TTL toDateTime(stat_date) + INTERVAL 365 DAY;
