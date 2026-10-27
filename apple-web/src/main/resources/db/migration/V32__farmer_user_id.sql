-- Add user_id to farm_farmer for direct user→farmer linkage
-- Enables "my orchards" query without cross-module dependency
ALTER TABLE farm_farmer ADD COLUMN user_id BIGINT NULL COMMENT '关联平台用户ID' AFTER id;

-- Backfill: match farm_farmer.phone → uc_user.phone
UPDATE farm_farmer f
    INNER JOIN uc_user u ON u.phone = f.phone
SET f.user_id = u.id
WHERE u.role_code = 'FARMER';

CREATE INDEX idx_farmer_user_id ON farm_farmer(user_id);
