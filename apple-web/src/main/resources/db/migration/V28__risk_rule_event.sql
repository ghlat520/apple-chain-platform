-- =============================================
-- V28: Risk Warning (规则 + 事件) — supports new
--      RiskRuleController / RiskEventController.
-- =============================================

-- 1. Risk warning rules
CREATE TABLE IF NOT EXISTS sf_risk_rule (
    id           BIGINT        PRIMARY KEY,
    name         VARCHAR(100)  NOT NULL COMMENT '规则名称',
    rule_type    VARCHAR(50)   NOT NULL COMMENT '规则类型 OVERDUE_DAYS/PRICE_DROP_PCT/QUALITY_FAIL_RATE/CONCENTRATION_RATIO/FRAUD_SCORE',
    threshold    DECIMAL(18,4) NOT NULL COMMENT '阈值（语义随 rule_type 变化）',
    severity     INT           NOT NULL COMMENT 'RiskSeverity.code 1=LOW 2=MEDIUM 3=HIGH 4=CRITICAL',
    enabled      TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '是否启用 1=启用 0=停用',
    creator_id   BIGINT        NULL     COMMENT '创建人用户ID',
    create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    VARCHAR(50)   NULL,
    deleted      INT           DEFAULT 0,
    INDEX idx_rule_severity (severity),
    INDEX idx_rule_enabled (enabled)
);

-- 2. Risk warning events triggered by rules
CREATE TABLE IF NOT EXISTS sf_risk_event (
    id            BIGINT        PRIMARY KEY,
    rule_id       BIGINT        NOT NULL COMMENT '关联 sf_risk_rule.id',
    target_type   VARCHAR(30)   NOT NULL COMMENT '业务对象类型 LOAN/PLEDGE/FARMER/COLD_CHAIN/...',
    target_id     BIGINT        NOT NULL COMMENT '业务对象ID',
    severity      INT           NOT NULL COMMENT 'RiskSeverity.code',
    trigger_value DECIMAL(18,4) NULL     COMMENT '触发时的指标实测值',
    status        INT           NOT NULL DEFAULT 1 COMMENT 'RiskEventStatus.code 1=PENDING 2=HANDLING 3=RESOLVED 4=IGNORED',
    assignee_id   BIGINT        NULL     COMMENT '处理人用户ID',
    handle_remark VARCHAR(500)  NULL     COMMENT '处置备注',
    trigger_time  DATETIME      NOT NULL COMMENT '触发时间',
    handle_time   DATETIME      NULL     COMMENT '处置时间（结案时填）',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     VARCHAR(50)   NULL,
    deleted       INT           DEFAULT 0,
    INDEX idx_event_status (status),
    INDEX idx_event_severity (severity),
    INDEX idx_event_rule (rule_id),
    INDEX idx_event_target (target_type, target_id)
);

-- =============================================
-- Seed: 10 risk rules
-- =============================================
INSERT INTO sf_risk_rule (id, name, rule_type, threshold, severity, enabled, creator_id) VALUES
 (2801, '贷款逾期超过30天',         'OVERDUE_DAYS',         30.0000, 3, 1, 1),
 (2802, '贷款逾期超过90天',         'OVERDUE_DAYS',         90.0000, 4, 1, 1),
 (2803, '苹果现货价格周跌幅>10%',   'PRICE_DROP_PCT',       10.0000, 2, 1, 1),
 (2804, '苹果现货价格周跌幅>20%',   'PRICE_DROP_PCT',       20.0000, 3, 1, 1),
 (2805, '冷链温度偏离>3度',         'TEMPERATURE_DEVIATION', 3.0000, 2, 1, 1),
 (2806, '质检不合格率>5%',          'QUALITY_FAIL_RATE',     5.0000, 3, 1, 1),
 (2807, '单一客户敞口>30%',         'CONCENTRATION_RATIO',  30.0000, 3, 1, 1),
 (2808, '欺诈评分>80分',            'FRAUD_SCORE',          80.0000, 4, 1, 1),
 (2809, '质押率>70%',               'PLEDGE_RATIO',         70.0000, 2, 1, 1),
 (2810, '信用评分<400分',           'CREDIT_SCORE_BELOW',  400.0000, 2, 0, 1);

-- =============================================
-- Seed: 30 risk events spanning all severities and lifecycle states
-- =============================================
INSERT INTO sf_risk_event (id, rule_id, target_type, target_id, severity, trigger_value, status, assignee_id, handle_remark, trigger_time, handle_time) VALUES
 (28001, 2801, 'LOAN',      10001, 3, 35.0000, 1, NULL, NULL,                              '2026-04-10 09:15:00', NULL),
 (28002, 2801, 'LOAN',      10002, 3, 42.0000, 2,    1, '已联系借款人,承诺7日内还款',      '2026-04-09 14:20:00', NULL),
 (28003, 2802, 'LOAN',      10003, 4, 95.0000, 1, NULL, NULL,                              '2026-04-11 08:00:00', NULL),
 (28004, 2802, 'LOAN',      10004, 4,120.0000, 2,    1, '已启动诉讼程序',                  '2026-04-08 10:30:00', NULL),
 (28005, 2803, 'PLEDGE',    20001, 2, 12.5000, 3,    1, '价格已回升,无需进一步处置',      '2026-04-05 15:00:00', '2026-04-12 11:00:00'),
 (28006, 2803, 'PLEDGE',    20002, 2, 11.2000, 1, NULL, NULL,                              '2026-04-12 09:00:00', NULL),
 (28007, 2804, 'PLEDGE',    20003, 3, 22.0000, 2,    1, '要求客户追加保证金',              '2026-04-11 16:45:00', NULL),
 (28008, 2804, 'PLEDGE',    20004, 3, 25.5000, 3,    1, '已追加保证金,风险解除',          '2026-04-06 09:30:00', '2026-04-10 18:00:00'),
 (28009, 2805, 'COLD_CHAIN',30001, 2,  3.5000, 4,    1, '设备校准误差,实际无影响',        '2026-04-09 02:15:00', '2026-04-09 09:00:00'),
 (28010, 2805, 'COLD_CHAIN',30002, 2,  4.2000, 1, NULL, NULL,                              '2026-04-12 03:45:00', NULL),
 (28011, 2806, 'FARMER',    40001, 3,  6.8000, 2,    1, '已通知质检整改',                  '2026-04-10 11:00:00', NULL),
 (28012, 2806, 'FARMER',    40002, 3,  8.5000, 1, NULL, NULL,                              '2026-04-11 14:00:00', NULL),
 (28013, 2807, 'LOAN',      10005, 3, 35.0000, 2,    1, '已制定客户敞口压降计划',          '2026-04-08 16:20:00', NULL),
 (28014, 2807, 'LOAN',      10006, 3, 42.0000, 4,    1, '历史敞口,业务已结清',            '2026-04-01 09:00:00', '2026-04-03 10:00:00'),
 (28015, 2808, 'FARMER',    40003, 4, 85.0000, 1, NULL, NULL,                              '2026-04-12 10:30:00', NULL),
 (28016, 2808, 'FARMER',    40004, 4, 92.0000, 2,    1, '已冻结账户,移交风控调查',         '2026-04-11 17:00:00', NULL),
 (28017, 2809, 'PLEDGE',    20005, 2, 75.0000, 3,    1, '已要求质押率降至65%',            '2026-04-07 13:45:00', '2026-04-09 16:30:00'),
 (28018, 2809, 'PLEDGE',    20006, 2, 72.5000, 1, NULL, NULL,                              '2026-04-12 11:00:00', NULL),
 (28019, 2810, 'FARMER',    40005, 2,380.0000, 3,    1, '建议提供担保人',                  '2026-04-05 10:00:00', '2026-04-08 11:00:00'),
 (28020, 2810, 'FARMER',    40006, 2,350.0000, 1, NULL, NULL,                              '2026-04-11 09:00:00', NULL),
 (28021, 2801, 'LOAN',      10007, 3, 31.0000, 1, NULL, NULL,                              '2026-04-12 08:00:00', NULL),
 (28022, 2801, 'LOAN',      10008, 3, 38.0000, 2,    1, '与客户协商展期',                  '2026-04-10 14:00:00', NULL),
 (28023, 2802, 'LOAN',      10009, 4,100.0000, 1, NULL, NULL,                              '2026-04-12 07:30:00', NULL),
 (28024, 2803, 'PLEDGE',    20007, 2, 13.5000, 4,    1, '价格已稳定,关闭事件',            '2026-04-04 10:00:00', '2026-04-11 09:00:00'),
 (28025, 2805, 'COLD_CHAIN',30003, 2,  3.8000, 2,    1, '已派工程师处理',                  '2026-04-11 22:30:00', NULL),
 (28026, 2806, 'FARMER',    40007, 3,  7.2000, 3,    1, '整改完成,质检合格',              '2026-04-06 10:00:00', '2026-04-12 14:00:00'),
 (28027, 2807, 'LOAN',      10010, 3, 31.5000, 1, NULL, NULL,                              '2026-04-12 09:30:00', NULL),
 (28028, 2808, 'FARMER',    40008, 4, 81.0000, 2,    1, '已上报风控部进一步调查',          '2026-04-11 18:30:00', NULL),
 (28029, 2809, 'PLEDGE',    20008, 2, 71.0000, 1, NULL, NULL,                              '2026-04-12 10:45:00', NULL),
 (28030, 2810, 'FARMER',    40009, 2,395.0000, 4,    1, '客户已结清相关贷款,事件作废',    '2026-04-02 09:00:00', '2026-04-09 10:30:00');
