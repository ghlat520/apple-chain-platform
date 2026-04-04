-- =============================================
-- V9: Supply-chain finance module tables
-- =============================================

-- 1. Credit Rating
CREATE TABLE IF NOT EXISTS sf_credit_rating (
    id              BIGINT       PRIMARY KEY,
    entity_type     VARCHAR(20)  NOT NULL COMMENT 'FARMER/ENTERPRISE/COOPERATIVE 评级主体类型',
    entity_id       BIGINT       NOT NULL COMMENT '主体ID（农户/企业）',
    entity_name     VARCHAR(100) NOT NULL COMMENT '主体名称',
    credit_score    INT          NOT NULL COMMENT '信用评分 0-1000',
    credit_level    VARCHAR(10)  NOT NULL COMMENT 'AAA/AA/A/BBB/BB/B/C',
    assessment_date DATE         NOT NULL COMMENT '评定日期',
    valid_until     DATE         NULL     COMMENT '有效期至',
    trade_score     INT          DEFAULT 0 COMMENT '交易信用分',
    production_score INT         DEFAULT 0 COMMENT '生产信用分',
    financial_score INT          DEFAULT 0 COMMENT '财务信用分',
    assessor        VARCHAR(50)  NULL     COMMENT '评定人',
    remark          VARCHAR(500) NULL,
    status          VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT 'ACTIVE/EXPIRED/REVOKED',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 2. Loan
CREATE TABLE IF NOT EXISTS sf_loan (
    id              BIGINT       PRIMARY KEY,
    loan_code       VARCHAR(32)  NOT NULL COMMENT '贷款编号 LN+日期+序号',
    borrower_name   VARCHAR(100) NOT NULL COMMENT '借款人名称',
    borrower_type   VARCHAR(20)  NOT NULL COMMENT 'FARMER/ENTERPRISE/COOPERATIVE',
    borrower_id     BIGINT       NULL     COMMENT '借款人ID',
    loan_type       VARCHAR(30)  NOT NULL COMMENT 'PLEDGE/RECEIVABLE/CREDIT 仓单质押/应收账款/信用贷',
    amount          DECIMAL(14,2) NOT NULL COMMENT '贷款金额(元)',
    interest_rate   DECIMAL(5,4) NOT NULL COMMENT '年利率',
    term_months     INT          NOT NULL COMMENT '期限(月)',
    apply_date      DATE         NOT NULL COMMENT '申请日期',
    approve_date    DATE         NULL     COMMENT '审批日期',
    disburse_date   DATE         NULL     COMMENT '放款日期',
    due_date        DATE         NULL     COMMENT '到期日期',
    repaid_amount   DECIMAL(14,2) DEFAULT 0 COMMENT '已还金额',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/DISBURSED/REPAID/OVERDUE',
    pledge_id       BIGINT       NULL     COMMENT '关联质押ID',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 3. Warehouse Receipt Pledge (仓单质押)
CREATE TABLE IF NOT EXISTS sf_pledge (
    id              BIGINT       PRIMARY KEY,
    pledge_code     VARCHAR(32)  NOT NULL COMMENT '质押编号 PL+日期+序号',
    receipt_id      BIGINT       NULL     COMMENT '关联仓单ID',
    receipt_code    VARCHAR(32)  NULL     COMMENT '仓单编码',
    pledgor_name    VARCHAR(100) NOT NULL COMMENT '出质人名称',
    pledgee_name    VARCHAR(100) NOT NULL COMMENT '质权人(银行/金融机构)',
    commodity       VARCHAR(100) NULL     COMMENT '质押物品',
    quantity        DECIMAL(12,2) NULL    COMMENT '质押数量',
    unit            VARCHAR(20)  NULL     COMMENT '单位',
    appraised_value DECIMAL(14,2) NULL    COMMENT '评估价值(元)',
    pledge_rate     DECIMAL(5,4) NULL     COMMENT '质押率',
    loan_amount     DECIMAL(14,2) NULL    COMMENT '可贷金额(元)',
    start_date      DATE         NULL     COMMENT '质押起始日',
    end_date        DATE         NULL     COMMENT '质押到期日',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/ACTIVE/RELEASED/DEFAULTED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- 4. Risk Record
CREATE TABLE IF NOT EXISTS sf_risk_record (
    id              BIGINT       PRIMARY KEY,
    risk_code       VARCHAR(32)  NOT NULL COMMENT '风控编号 RK+日期+序号',
    related_type    VARCHAR(20)  NOT NULL COMMENT 'LOAN/PLEDGE/CREDIT 关联类型',
    related_id      BIGINT       NULL     COMMENT '关联ID',
    risk_level      VARCHAR(10)  NOT NULL COMMENT 'HIGH/MEDIUM/LOW',
    risk_type       VARCHAR(50)  NOT NULL COMMENT 'OVERDUE/PRICE_DROP/QUALITY/FRAUD 风险类型',
    description     VARCHAR(500) NOT NULL COMMENT '风险描述',
    measure         VARCHAR(500) NULL     COMMENT '处置措施',
    handler         VARCHAR(50)  NULL     COMMENT '处理人',
    handle_time     DATETIME     NULL     COMMENT '处理时间',
    status          VARCHAR(20)  DEFAULT 'OPEN' COMMENT 'OPEN/HANDLING/RESOLVED/CLOSED',
    remark          VARCHAR(500) NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       VARCHAR(50)  NULL,
    deleted         INT          DEFAULT 0
);

-- =============================================
-- Seed data
-- =============================================

-- Credit Ratings
INSERT INTO sf_credit_rating (id, entity_type, entity_id, entity_name, credit_score, credit_level, assessment_date, valid_until, trade_score, production_score, financial_score, assessor, status) VALUES
(9001, 'FARMER',     3001, '张三丰', 850, 'AAA', '2026-01-15', '2027-01-15', 280, 290, 280, '信用评估中心', 'ACTIVE'),
(9002, 'FARMER',     3002, '李莫愁', 720, 'AA',  '2026-01-20', '2027-01-20', 240, 250, 230, '信用评估中心', 'ACTIVE'),
(9003, 'ENTERPRISE', 5001, '洛川苹果合作社', 910, 'AAA', '2026-02-01', '2027-02-01', 300, 310, 300, '信用评估中心', 'ACTIVE'),
(9004, 'FARMER',     3003, '王重阳', 580, 'BB',  '2025-06-01', '2026-06-01', 200, 190, 190, '信用评估中心', 'ACTIVE'),
(9005, 'COOPERATIVE',5002, '白水果业联盟', 680, 'A',   '2026-03-01', '2027-03-01', 230, 220, 230, '信用评估中心', 'ACTIVE');

-- Loans
INSERT INTO sf_loan (id, loan_code, borrower_name, borrower_type, loan_type, amount, interest_rate, term_months, apply_date, approve_date, disburse_date, due_date, repaid_amount, status) VALUES
(9101, 'LN202603010001', '张三丰', 'FARMER', 'PLEDGE', 200000.00, 0.0485, 12, '2026-03-01', '2026-03-05', '2026-03-06', '2027-03-06', 0.00, 'DISBURSED'),
(9102, 'LN202603020001', '洛川苹果合作社', 'ENTERPRISE', 'RECEIVABLE', 500000.00, 0.0520, 6, '2026-03-02', '2026-03-08', NULL, NULL, 0.00, 'APPROVED'),
(9103, 'LN202603030001', '李莫愁', 'FARMER', 'CREDIT', 80000.00, 0.0550, 12, '2026-03-03', NULL, NULL, NULL, 0.00, 'PENDING'),
(9104, 'LN202602150001', '王重阳', 'FARMER', 'CREDIT', 50000.00, 0.0600, 6, '2026-02-15', '2026-02-20', '2026-02-21', '2026-08-21', 50000.00, 'REPAID');

-- Pledges
INSERT INTO sf_pledge (id, pledge_code, receipt_code, pledgor_name, pledgee_name, commodity, quantity, unit, appraised_value, pledge_rate, loan_amount, start_date, end_date, status) VALUES
(9201, 'PL202603010001', 'WR202603010001', '张三丰', '陕西农商银行', '红富士苹果(一级)', 50000.00, 'kg', 400000.00, 0.5000, 200000.00, '2026-03-06', '2027-03-06', 'ACTIVE'),
(9202, 'PL202603050001', 'WR202603020001', '洛川苹果合作社', '中国农业银行', '嘎啦苹果(特级)', 80000.00, 'kg', 720000.00, 0.6000, 432000.00, NULL, NULL, 'PENDING'),
(9203, 'PL202602100001', 'WR202602100001', '白水果业联盟', '西安银行', '秦冠苹果(二级)', 30000.00, 'kg', 180000.00, 0.5000, 90000.00, '2026-02-10', '2026-08-10', 'RELEASED');

-- Risk Records
INSERT INTO sf_risk_record (id, risk_code, related_type, related_id, risk_level, risk_type, description, measure, handler, handle_time, status) VALUES
(9301, 'RK202603150001', 'LOAN', 9101, 'LOW', 'PRICE_DROP', '苹果市场价格下跌8%，质押物价值略有缩水', '持续监控，暂不调整质押率', '风控专员A', '2026-03-15 10:00:00', 'RESOLVED'),
(9302, 'RK202603200001', 'PLEDGE', 9201, 'MEDIUM', 'QUALITY', '质押苹果部分出现轻微损耗，需复检', '安排仓库复检，评估损耗比例', NULL, NULL, 'OPEN'),
(9303, 'RK202603250001', 'LOAN', 9103, 'HIGH', 'OVERDUE', '借款人信用评分偏低(BB级)，贷款申请需额外审核', '要求提供追加担保或降低贷款额度', '风控专员B', '2026-03-25 14:00:00', 'HANDLING');
