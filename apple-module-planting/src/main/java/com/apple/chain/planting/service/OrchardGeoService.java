package com.apple.chain.planting.service;

import com.apple.chain.planting.dto.OrchardGeoVO;
import com.apple.chain.planting.entity.Orchard;

import java.util.List;

/**
 * M4 GIS map service: orchard boundary management + viewport queries.
 *
 * Kept separate from {@link OrchardService} so the GIS code path stays
 * focused and the existing CRUD service is unchanged.
 */
public interface OrchardGeoService {

    /**
     * Viewport (bounding-box) query.
     * Coordinates are EPSG:4326 (WGS84) decimal degrees.
     */
    List<OrchardGeoVO> findInBoundingBox(double lng1, double lat1, double lng2, double lat2);

    /**
     * Save the orchard polygon boundary.
     * Computes centroid + area (亩) server-side from the GeoJSON and persists all three.
     *
     * @return the updated orchard (with centroid + areaMu populated)
     */
    Orchard saveBoundary(Long orchardId, String geoJson);
}
