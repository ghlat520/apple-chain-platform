-- =============================================
-- V8: Cold-chain logistics module tables
-- =============================================

-- 1. Vehicles
CREATE TABLE IF NOT EXISTS cc_vehicle (
    id              BIGINT       PRIMARY KEY,
    vehicle_code    VARCHAR(32)  NOT NULL COMMENT '车辆编码 VH+日期+序号',
    plate_number    VARCHAR(20)  NOT NULL COMMENT '车牌号',
    vehicle_type    VARCHAR(20)  NOT NULL COMMENT 'REFRIGERATED/INSULATED/NORMAL 冷藏/保温/普通',
    brand           VARCHAR(50)  NULL     COMMENT '品牌型号',
    capacity        DECIMAL(10,2) NULL    COMMENT '载重(吨)',
    volume          DECIMAL(10,2) NULL    COMMENT '容积(m3)',
    temperature_min DECIMAL(5,2)  NULL    COMMENT '最低控温(C)',
    temperature_max DECIMAL(5,2)  NULL    COMMENT '最高控温(C)',
    driver_name     VARCHAR(50)  NULL     COMMENT '驾驶员',
    driver_phone    VARCHAR(20)  NULL     COMMENT '驾驶员电话',
    status          VARCHAR(20)  DEFAULT 'IDLE' COMMENT 'IDLE/IN_TRANSIT/MAINTENANCE/RETIRED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 2. Transport tasks
CREATE TABLE IF NOT EXISTS cc_transport_task (
    id              BIGINT       PRIMARY KEY,
    task_code       VARCHAR(32)  NOT NULL COMMENT '任务编码 TT+日期+序号',
    vehicle_id      BIGINT       NULL     COMMENT '关联车辆ID',
    order_id        BIGINT       NULL     COMMENT '关联订单ID',
    origin          VARCHAR(200) NOT NULL COMMENT '出发地',
    destination     VARCHAR(200) NOT NULL COMMENT '目的地',
    cargo_desc      VARCHAR(200) NULL     COMMENT '货物描述',
    cargo_weight    DECIMAL(10,2) NULL    COMMENT '货物重量(kg)',
    required_temp   DECIMAL(5,2)  NULL    COMMENT '要求温度(C)',
    plan_depart     DATETIME     NULL     COMMENT '计划发车时间',
    actual_depart   DATETIME     NULL     COMMENT '实际发车时间',
    plan_arrive     DATETIME     NULL     COMMENT '计划到达时间',
    actual_arrive   DATETIME     NULL     COMMENT '实际到达时间',
    distance        DECIMAL(10,2) NULL    COMMENT '运输距离(km)',
    cost            DECIMAL(12,2) NULL    COMMENT '运输费用',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/IN_TRANSIT/DELIVERED/CANCELLED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 3. Temperature records (IoT simulation)
CREATE TABLE IF NOT EXISTS cc_temperature_record (
    id              BIGINT       PRIMARY KEY,
    task_id         BIGINT       NOT NULL COMMENT '关联运输任务ID',
    vehicle_id      BIGINT       NULL     COMMENT '关联车辆ID',
    temperature     DECIMAL(5,2) NOT NULL COMMENT '记录温度(C)',
    humidity        DECIMAL(5,2) NULL     COMMENT '记录湿度(%)',
    location        VARCHAR(200) NULL     COMMENT '记录位置',
    record_time     DATETIME     NOT NULL COMMENT '记录时间',
    is_alarm        INT          DEFAULT 0 COMMENT '是否超温报警 0否1是',
    alarm_msg       VARCHAR(200) NULL     COMMENT '报警信息',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 4. Delivery / sign-off
CREATE TABLE IF NOT EXISTS cc_delivery (
    id              BIGINT       PRIMARY KEY,
    delivery_code   VARCHAR(32)  NOT NULL COMMENT '配送编码 DL+日期+序号',
    task_id         BIGINT       NOT NULL COMMENT '关联运输任务ID',
    receiver_name   VARCHAR(50)  NOT NULL COMMENT '收货人',
    receiver_phone  VARCHAR(20)  NULL     COMMENT '收货人电话',
    receiver_addr   VARCHAR(200) NULL     COMMENT '收货地址',
    delivery_time   DATETIME     NULL     COMMENT '配送时间',
    sign_time       DATETIME     NULL     COMMENT '签收时间',
    sign_photo      VARCHAR(500) NULL     COMMENT '签收照片URL',
    quality_check   VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/PASSED/REJECTED 质检',
    quality_remark  VARCHAR(500) NULL     COMMENT '质检备注',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/DELIVERING/SIGNED/REJECTED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- =============================================
-- Seed data
-- =============================================

-- Vehicles
INSERT INTO cc_vehicle (id, vehicle_code, plate_number, vehicle_type, brand, capacity, volume, temperature_min, temperature_max, driver_name, driver_phone, status) VALUES
(8001, 'VH202603010001', '陕A12345', 'REFRIGERATED', '福田欧曼', 10.00, 28.00, -5.00, 8.00,  '张师傅', '13900001001', 'IDLE'),
(8002, 'VH202603010002', '陕A23456', 'REFRIGERATED', '解放J6P',  15.00, 40.00, -8.00, 5.00,  '李师傅', '13900001002', 'IN_TRANSIT'),
(8003, 'VH202603010003', '陕A34567', 'INSULATED',    '东风天龙', 12.00, 32.00,  0.00, 15.00, '王师傅', '13900001003', 'IDLE'),
(8004, 'VH202603010004', '陕A45678', 'NORMAL',       '江淮格尔发', 8.00, 22.00, NULL,  NULL,  '赵师傅', '13900001004', 'MAINTENANCE');

-- Transport tasks
INSERT INTO cc_transport_task (id, task_code, vehicle_id, origin, destination, cargo_desc, cargo_weight, required_temp, plan_depart, actual_depart, plan_arrive, actual_arrive, distance, cost, status) VALUES
(8101, 'TT202603010001', 8002, '延安市洛川县果园基地', '西安市雁塔区批发市场', '红富士苹果 500箱', 2500.00, 2.00, '2026-03-01 06:00:00', '2026-03-01 06:30:00', '2026-03-01 12:00:00', NULL, 320.00, 3500.00, 'IN_TRANSIT'),
(8102, 'TT202603020001', 8001, '咸阳市乾县冷库',     '西安市未央区超市配送中心', '嘎啦苹果 300箱', 1500.00, 3.00, '2026-03-02 07:00:00', '2026-03-02 07:15:00', '2026-03-02 10:00:00', '2026-03-02 09:50:00', 80.00, 1200.00, 'DELIVERED'),
(8103, 'TT202603030001', 8003, '宝鸡市凤翔区果园',   '成都市龙泉驿区水果批发市场', '秦冠苹果 800箱', 4000.00, 5.00, '2026-03-05 05:00:00', NULL, '2026-03-05 18:00:00', NULL, 580.00, 6800.00, 'PENDING'),
(8104, 'TT202603040001', 8001, '渭南市白水县冷库',   '北京市新发地批发市场', '红富士苹果 1200箱', 6000.00, 1.00, '2026-03-06 04:00:00', NULL, '2026-03-07 08:00:00', NULL, 1050.00, 12000.00, 'PENDING');

-- Temperature records (for in-transit task 8101)
INSERT INTO cc_temperature_record (id, task_id, vehicle_id, temperature, humidity, location, record_time, is_alarm, alarm_msg) VALUES
(8201, 8101, 8002, 2.10, 85.0, '延安市洛川县', '2026-03-01 06:30:00', 0, NULL),
(8202, 8101, 8002, 2.30, 84.0, '铜川市耀州区', '2026-03-01 08:00:00', 0, NULL),
(8203, 8101, 8002, 5.80, 82.0, '咸阳市三原县', '2026-03-01 09:30:00', 1, '温度偏高，超过要求温度2C，已达5.8C'),
(8204, 8101, 8002, 2.50, 83.0, '西安市临潼区', '2026-03-01 10:30:00', 0, NULL),
(8205, 8102, 8001, 3.20, 86.0, '咸阳市乾县',   '2026-03-02 07:15:00', 0, NULL),
(8206, 8102, 8001, 3.00, 85.0, '西安市未央区', '2026-03-02 09:50:00', 0, NULL);

-- Delivery / sign-off
INSERT INTO cc_delivery (id, delivery_code, task_id, receiver_name, receiver_phone, receiver_addr, delivery_time, sign_time, quality_check, status) VALUES
(8301, 'DL202603020001', 8102, '王经理', '13800002001', '西安市未央区凤城五路超市配送中心', '2026-03-02 09:50:00', '2026-03-02 10:05:00', 'PASSED', 'SIGNED'),
(8302, 'DL202603010001', 8101, '刘主任', '13800002002', '西安市雁塔区电子正街批发市场A区', NULL, NULL, 'PENDING', 'PENDING'),
(8303, 'DL202603050001', 8103, '陈老板', '13800002003', '成都市龙泉驿区国际水果批发城', NULL, NULL, 'PENDING', 'PENDING');
