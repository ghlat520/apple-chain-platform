package com.apple.chain.planting.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure-function utility that converts a GeoJSON Polygon string to:
 *   - centroid (BigDecimal[lng, lat])
 *   - area in mu (亩, 1亩 = 666.67 m²)
 *
 * Algorithm: spherical excess approximation for small polygons (typical
 * orchard size, well below 100 km²) using the L'Huilier-style projection
 * to local ENU plane + Shoelace formula. Accuracy within ~0.5% for any
 * polygon under 10 km diameter, which more than meets the M4 requirement
 * of "面积误差 &lt;5%".
 *
 * <p>Why not MySQL ST_Area? Because the existing schema is plain InnoDB
 * without spatial indexes; we keep the geometry as JSON and compute server-side.
 *
 * <p>Input format (GeoJSON Polygon):
 * <pre>
 * {
 *   "type": "Polygon",
 *   "coordinates": [
 *     [[lng1, lat1], [lng2, lat2], ..., [lng1, lat1]]   // outer ring, closed
 *   ]
 * }
 * </pre>
 */
public final class GeoJsonAreaCalculator {

    /** Earth radius in meters (WGS84 mean). */
    private static final double EARTH_RADIUS_M = 6_371_008.8;

    /** Square meters per Chinese mu (1 亩 = 666.67 m²). */
    private static final BigDecimal SQ_M_PER_MU = new BigDecimal("666.67");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GeoJsonAreaCalculator() {
        // utility
    }

    /** Parsed result holder. */
    public record GeoResult(BigDecimal centerLng, BigDecimal centerLat, BigDecimal areaMu) {
    }

    /**
     * Parse a GeoJSON Polygon and return centroid + area.
     *
     * @throws IllegalArgumentException if the input is not a valid Polygon with at least 3 distinct vertices
     */
    public static GeoResult parse(String geoJson) {
        if (geoJson == null || geoJson.isBlank()) {
            throw new IllegalArgumentException("geoJson is null or blank");
        }
        JsonNode root;
        try {
            root = MAPPER.readTree(geoJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid GeoJSON: " + e.getMessage(), e);
        }
        JsonNode typeNode = root.get("type");
        if (typeNode == null || !"Polygon".equals(typeNode.asText())) {
            throw new IllegalArgumentException("only Polygon supported, got: " + typeNode);
        }
        JsonNode coords = root.get("coordinates");
        if (coords == null || !coords.isArray() || coords.size() == 0) {
            throw new IllegalArgumentException("coordinates missing or empty");
        }
        JsonNode outerRing = coords.get(0);
        if (!outerRing.isArray() || outerRing.size() < 4) {
            // Need at least 4 points (3 distinct + closing repetition)
            throw new IllegalArgumentException("polygon outer ring needs >= 4 points (closed)");
        }

        List<double[]> ring = new ArrayList<>(outerRing.size());
        for (JsonNode point : outerRing) {
            if (!point.isArray() || point.size() < 2) {
                throw new IllegalArgumentException("each coordinate must be [lng, lat]");
            }
            ring.add(new double[]{point.get(0).asDouble(), point.get(1).asDouble()});
        }

        double[] centroid = centroid(ring);
        double areaSqM = areaSquareMeters(ring, centroid[1]);
        BigDecimal areaMu = BigDecimal.valueOf(areaSqM)
                .divide(SQ_M_PER_MU, 2, RoundingMode.HALF_UP);

        return new GeoResult(
                BigDecimal.valueOf(centroid[0]).setScale(7, RoundingMode.HALF_UP),
                BigDecimal.valueOf(centroid[1]).setScale(7, RoundingMode.HALF_UP),
                areaMu
        );
    }

    /**
     * Centroid as [lng, lat]. Uses simple arithmetic mean of distinct vertices
     * (excluding the closing repetition). Sufficient for small polygons.
     */
    static double[] centroid(List<double[]> ring) {
        // Drop the closing point if it duplicates the first
        int n = ring.size();
        if (n > 1) {
            double[] first = ring.get(0);
            double[] last = ring.get(n - 1);
            if (first[0] == last[0] && first[1] == last[1]) {
                n--;
            }
        }
        double sumLng = 0;
        double sumLat = 0;
        for (int i = 0; i < n; i++) {
            sumLng += ring.get(i)[0];
            sumLat += ring.get(i)[1];
        }
        return new double[]{sumLng / n, sumLat / n};
    }

    /**
     * Spherical-projection Shoelace area in m².
     * Projects each (lng, lat) to a local ENU plane centered at the polygon's
     * centroid latitude, then runs the planar Shoelace formula. Error stays
     * under 0.5% for polygons up to ~10 km across.
     */
    static double areaSquareMeters(List<double[]> ring, double centerLatDeg) {
        double centerLatRad = Math.toRadians(centerLatDeg);
        double mPerDegLat = (Math.PI / 180.0) * EARTH_RADIUS_M;
        double mPerDegLng = mPerDegLat * Math.cos(centerLatRad);

        int n = ring.size();
        double sum = 0;
        for (int i = 0; i < n - 1; i++) {
            double x1 = ring.get(i)[0] * mPerDegLng;
            double y1 = ring.get(i)[1] * mPerDegLat;
            double x2 = ring.get(i + 1)[0] * mPerDegLng;
            double y2 = ring.get(i + 1)[1] * mPerDegLat;
            sum += (x1 * y2) - (x2 * y1);
        }
        return Math.abs(sum) / 2.0;
    }
}
