-- =============================================================================
-- V10 - M1 Six-Role RBAC: roles, permissions, user-role, role-permission, data scopes
-- Spec: docs/pipeline/apple-chain-platform/design-patches-12-missing.md (M1)
--
-- Decisions vs spec:
--   1. Migration number is V10 (next free slot), not V12 — V10 is the next gap.
--   2. role_code keeps the EXISTING uc_user.role_code naming (no ROLE_ prefix)
--      to stay backward-compatible with existing seeded users (admin/farmer01/buyer01)
--      and the JWT roleCode claim already in production.
--   3. Six business roles + ADMIN = 7 rows in sys_role
--      (FARMER, SUPPLIER, BUYER, LOGISTICS, FINANCE, GOV, ADMIN).
--   4. Permission codes follow spec format: <resource>:<action>.
-- =============================================================================

-- ===== sys_role: role definitions =====
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT          NOT NULL,
    `role_code`   VARCHAR(32)     NOT NULL,
    `role_name`   VARCHAR(64)     NOT NULL,
    `description` VARCHAR(255),
    `sort_order`  INT             NOT NULL DEFAULT 0,
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(64),
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) COMMENT='RBAC role definitions';

-- ===== sys_permission: permission point catalog =====
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`          BIGINT          NOT NULL,
    `perm_code`   VARCHAR(64)     NOT NULL,
    `resource`    VARCHAR(32)     NOT NULL,
    `action`      VARCHAR(16)     NOT NULL,
    `description` VARCHAR(255),
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `create_by`   VARCHAR(64),
    `deleted`     TINYINT         NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`),
    KEY `idx_resource` (`resource`)
) COMMENT='RBAC permission catalog';

-- ===== sys_role_permission: role <-> permission mapping =====
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `role_id`     BIGINT          NOT NULL,
    `perm_id`     BIGINT          NOT NULL,
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`role_id`, `perm_id`)
) COMMENT='RBAC role-permission mapping';

-- ===== sys_user_role: user <-> role mapping (1 user can hold N roles) =====
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id`     BIGINT          NOT NULL,
    `role_id`     BIGINT          NOT NULL,
    `grant_time`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `grant_by`    VARCHAR(64),
    PRIMARY KEY (`user_id`, `role_id`)
) COMMENT='RBAC user-role mapping';

-- ===== sys_user_data_scope: per-user data visibility =====
CREATE TABLE IF NOT EXISTS `sys_user_data_scope` (
    `user_id`     BIGINT          NOT NULL,
    `scope_type`  VARCHAR(16)     NOT NULL COMMENT 'GLOBAL/PROVINCE/CITY/OWN',
    `scope_value` VARCHAR(64)              COMMENT 'e.g. 山东 or 烟台 or NULL for GLOBAL/OWN',
    `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) COMMENT='RBAC data-scope per user';

-- =============================================================================
-- Seed: 7 roles (6 business + ADMIN)
-- =============================================================================
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `sort_order`) VALUES
(1, 'ADMIN',     '系统管理员',      '平台运营方，全部权限',                            1),
(2, 'FARMER',    '种植户/合作社',   '果园档案、作业记录、农资采购、供应发布、种植贷',  10),
(3, 'SUPPLIER',  '供应商/加工方',   '加工溯源、质检上传、仓储入库',                    20),
(4, 'BUYER',     '收购商/经销商',   '需求发布、撮合对接、线上交易、交易贷/出口贷',     30),
(5, 'LOGISTICS', '物流服务商',      '资源入驻、调度接单、温控追踪',                    40),
(6, 'FINANCE',   '金融机构',        '信用查询、融资审核、放款、风控监控',              50),
(7, 'GOV',       '监管部门',        '全产业链数据查询、异常追溯、统计分析、处罚记录',  60);

-- =============================================================================
-- Seed: permission catalog (resource:action)
-- =============================================================================
INSERT INTO `sys_permission` (`id`, `perm_code`, `resource`, `action`, `description`) VALUES
-- orchard / farm
(100, 'orchard:read',   'orchard', 'read',   '果园档案查看'),
(101, 'orchard:write',  'orchard', 'write',  '果园档案编辑'),
(102, 'orchard:delete', 'orchard', 'delete', '果园档案删除'),
-- cultivation
(110, 'cultivation:read',  'cultivation', 'read',  '种植/作业记录查看'),
(111, 'cultivation:write', 'cultivation', 'write', '种植/作业记录编辑'),
-- input (agri-input)
(120, 'input:read',   'input', 'read',   '农资查看'),
(121, 'input:write',  'input', 'write',  '农资采购'),
-- supply / trade
(130, 'supply:read',  'supply', 'read',  '供应信息查看'),
(131, 'supply:write', 'supply', 'write', '供应信息发布'),
(140, 'trade:read',    'trade', 'read',    '订单查看'),
(141, 'trade:write',   'trade', 'write',   '订单创建/编辑'),
(142, 'trade:approve', 'trade', 'approve', '订单审批/确认'),
-- warehouse
(150, 'warehouse:read',  'warehouse', 'read',  '仓储查看'),
(151, 'warehouse:write', 'warehouse', 'write', '仓储入库/出库'),
-- coldchain / logistics
(160, 'logistics:read',  'logistics', 'read',  '物流查看'),
(161, 'logistics:write', 'logistics', 'write', '物流调度'),
-- finance
(170, 'finance:read',    'finance', 'read',    '金融数据查看'),
(171, 'finance:write',   'finance', 'write',   '融资申请/审核'),
(172, 'finance:approve', 'finance', 'approve', '融资放款'),
-- trace
(180, 'trace:read',  'trace', 'read',  '溯源查看'),
(181, 'trace:write', 'trace', 'write', '溯源记录上传'),
-- regulator / stats
(190, 'stats:read',     'stats',     'read',  '统计分析查看'),
(191, 'penalty:write',  'penalty',   'write', '处罚记录登记'),
-- admin / user mgmt
(900, 'user:read',   'user', 'read',   '用户查看'),
(901, 'user:write',  'user', 'write',  '用户编辑'),
(902, 'user:delete', 'user', 'delete', '用户删除'),
(910, 'role:read',   'role', 'read',   '角色查看'),
(911, 'role:write',  'role', 'write',  '角色与权限分配');

-- =============================================================================
-- Seed: role-permission grants
-- =============================================================================

-- ADMIN: all permissions
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`)
SELECT 1, id FROM `sys_permission`;

-- FARMER: orchard, cultivation, input, supply (write), finance (read+write self)
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(2, 100), (2, 101),
(2, 110), (2, 111),
(2, 120), (2, 121),
(2, 130), (2, 131),
(2, 140), (2, 141),
(2, 170), (2, 171),
(2, 180), (2, 181);

-- SUPPLIER: trace write, warehouse, supply
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(3, 130), (3, 131),
(3, 150), (3, 151),
(3, 180), (3, 181),
(3, 110), (3, 111);

-- BUYER: trade RW + approve, supply read, finance read+write (trade-loan/export-loan)
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(4, 130),
(4, 140), (4, 141), (4, 142),
(4, 170), (4, 171),
(4, 180);

-- LOGISTICS: logistics RW, warehouse read, trace read
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(5, 160), (5, 161),
(5, 150),
(5, 180);

-- FINANCE: finance read+approve, trade read, stats read
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(6, 170), (6, 171), (6, 172),
(6, 140),
(6, 190);

-- GOV / regulator: read all + penalty write + stats
INSERT INTO `sys_role_permission` (`role_id`, `perm_id`) VALUES
(7, 100), (7, 110), (7, 120), (7, 130),
(7, 140), (7, 150), (7, 160), (7, 170),
(7, 180),
(7, 190), (7, 191);

-- =============================================================================
-- Seed: user-role mapping for existing seeded users (uc_user from V4)
-- =============================================================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `grant_by`) VALUES
(1001, 1, 'system'),  -- admin    -> ADMIN
(1002, 2, 'system'),  -- farmer01 -> FARMER
(1003, 4, 'system');  -- buyer01  -> BUYER

-- Default data scopes
INSERT INTO `sys_user_data_scope` (`user_id`, `scope_type`, `scope_value`) VALUES
(1001, 'GLOBAL',  NULL),
(1002, 'OWN',     NULL),
(1003, 'OWN',     NULL);
