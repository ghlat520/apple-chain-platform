-- =============================================================================
-- V15 - M4 GIS map: orchard geo fields (center point, polygon boundary, area)
-- Reuses existing pt_orchard.longitude/latitude as the marker; adds polygon
-- boundary as JSON (MySQL 8 native JSON type) plus a derived area in mu.
--
-- Decisions vs spec:
--   1. Use JSON type instead of MySQL spatial GEOMETRY because:
--      - Existing schema is plain InnoDB, no SRID setup
--      - Frontend produces GeoJSON natively (高德 polygon → GeoJSON)
--      - Bbox queries are still performant with the derived center point + index
--   2. center_lat/center_lng are denormalized from boundary centroid for
--      O(1) bbox queries without parsing JSON each time
--   3. area_mu is computed server-side via Shoelace formula on save (亩 = 666.67 m²)
-- =============================================================================

ALTER TABLE `pt_orchard`
    ADD COLUMN `latitude`          DECIMAL(10,7) NULL COMMENT '纬度' AFTER `area`,
    ADD COLUMN `longitude`         DECIMAL(10,7) NULL COMMENT '经度' AFTER `latitude`,
    ADD COLUMN `center_lat`        DECIMAL(10,7) NULL COMMENT 'GIS 中心点纬度（边界质心）' AFTER `longitude`,
    ADD COLUMN `center_lng`        DECIMAL(10,7) NULL COMMENT 'GIS 中心点经度（边界质心）' AFTER `center_lat`,
    ADD COLUMN `boundary_geojson`  JSON          NULL COMMENT '地块边界 GeoJSON Polygon' AFTER `center_lng`,
    ADD COLUMN `area_mu`           DECIMAL(12,2) NULL COMMENT '由边界自动计算的面积（亩）' AFTER `boundary_geojson`;

-- Index for bbox/viewport queries (center point lookup)
CREATE INDEX `idx_pt_orchard_center` ON `pt_orchard` (`center_lat`, `center_lng`);
