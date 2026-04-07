-- =============================================================================
-- V21 - M3 5G+北斗 IoT mock + 告警
--
-- Three tables. iot_telemetry uses (device_sn, collect_time) as a unique key
-- to make the ingest endpoint idempotent (replays from edge gateway during
-- reconnect cycles).
-- =============================================================================

CREATE TABLE IF NOT EXISTS `iot_device` (
    `id`                BIGINT       NOT NULL,
    `device_sn`         VARCHAR(64)  NOT NULL,
    `device_type`       VARCHAR(16)  NOT NULL COMMENT 'COLD_TRUCK / WAREHOUSE / PRE_COOLING',
    `vehicle_no`        VARCHAR(16)           COMMENT '关联车牌号',
    `warehouse_id`      BIGINT,
    `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '0=离线 1=在线 2=故障',
    `last_online_time`  DATETIME,
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by`         VARCHAR(64),
    `deleted`           TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_sn` (`device_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M3 IoT 设备';

CREATE TABLE IF NOT EXISTS `iot_telemetry` (
    `id`             BIGINT       NOT NULL,
    `device_sn`      VARCHAR(64)  NOT NULL,
    `collect_time`   DATETIME     NOT NULL,
    `latitude`       DECIMAL(10,7),
    `longitude`      DECIMAL(10,7),
    `altitude`       DECIMAL(8,2),
    `speed`          DECIMAL(6,2) COMMENT 'km/h',
    `temp_1`         DECIMAL(5,2),
    `temp_2`         DECIMAL(5,2),
    `temp_3`         DECIMAL(5,2),
    `temp_4`         DECIMAL(5,2),
    `humidity`       DECIMAL(5,2),
    `door_status`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0=关 1=开',
    `signal_type`    VARCHAR(8)   COMMENT '5G/4G/OFFLINE',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_time` (`device_sn`, `collect_time`),
    KEY `idx_device_time` (`device_sn`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M3 IoT 遥测';
-- Production note: partition by RANGE (TO_DAYS(collect_time)) when row count > 10M.

CREATE TABLE IF NOT EXISTS `iot_alert` (
    `id`             BIGINT       NOT NULL,
    `device_sn`      VARCHAR(64),
    `alert_type`     VARCHAR(32)  NOT NULL COMMENT 'TEMP_HIGH/TEMP_LOW/DOOR_ANOMALY/OFFLINE/SENSOR_FAULT',
    `alert_level`    TINYINT      NOT NULL DEFAULT 2 COMMENT '1=提醒 2=警告 3=严重',
    `threshold_value` VARCHAR(32),
    `actual_value`   VARCHAR(32),
    `message`        VARCHAR(512),
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `handled`        TINYINT      NOT NULL DEFAULT 0,
    `deleted`        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_device_type` (`device_sn`, `alert_type`),
    KEY `idx_handled` (`handled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='M3 IoT 告警';
