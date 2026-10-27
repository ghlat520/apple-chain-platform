-- =============================================================================
-- V27 - Backfill area_mu from area column
--
-- V15 added area_mu column but V2 seed data predates it.
-- area_mu (亩) is the same unit as area in this dataset, so we copy directly.
-- =============================================================================

UPDATE `farm_orchard`
SET `area_mu` = `area`
WHERE `area_mu` IS NULL
  AND `area` IS NOT NULL;
