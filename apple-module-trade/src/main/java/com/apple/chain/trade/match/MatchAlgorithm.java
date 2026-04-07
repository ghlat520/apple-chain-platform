package com.apple.chain.trade.match;

import com.apple.chain.trade.entity.PurchaseNeed;
import com.apple.chain.trade.entity.SupplyInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * M7 pure-function matching algorithm.
 *
 * <p>4-dimension weighted score (geo dimension dropped because the existing
 * SupplyInfo/PurchaseNeed entities don't store coordinates — they only have
 * free-form location strings; adding coordinates is a separate ALTER and
 * would expand PR#4 scope unnecessarily).
 *
 * <pre>
 *   score = 0.40 * variety_match           // 1 if variety matches, 0 otherwise
 *         + 0.30 * quality_compatible      // supply quality &gt;= demand min quality
 *         + 0.25 * price_overlap           // demand max &gt;= supply expected
 *         + 0.05 * quantity_fit            // supply qty &gt;= demand qty
 *   final = score * 100   (0-100 range)
 * </pre>
 *
 * Variety mismatch is a hard NO — score returns 0 immediately.
 */
public final class MatchAlgorithm {

    /** Quality grade ordering: A &gt; B &gt; C. Higher rank = better. */
    private static final java.util.Map<String, Integer> QUALITY_RANK = java.util.Map.of(
            "A", 3, "B", 2, "C", 1
    );

    private MatchAlgorithm() {
        // utility
    }

    /**
     * Compute the match score (0-100). Returns 0 for varieties that don't match
     * (hard reject) so callers can simply filter {@code score &gt; 0}.
     */
    public static BigDecimal score(SupplyInfo supply, PurchaseNeed demand) {
        if (supply == null || demand == null) {
            return BigDecimal.ZERO;
        }
        // Variety: hard match
        if (supply.getVariety() == null || demand.getVariety() == null
                || !supply.getVariety().equals(demand.getVariety())) {
            return BigDecimal.ZERO;
        }
        double total =
                0.40 * 1.0
              + 0.30 * qualityScore(supply.getQuality(), demand.getQuality())
              + 0.25 * priceScore(supply.getPriceExpected(), demand.getPriceMax())
              + 0.05 * quantityScore(supply.getQuantity(), demand.getQuantity());
        return BigDecimal.valueOf(total * 100.0).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Quality score: 1.0 if supply >= demand quality, 0.5 if one rank below, 0 otherwise.
     * Null on either side defaults to 0.5 (neutral).
     */
    static double qualityScore(String supplyQuality, String demandMinQuality) {
        if (supplyQuality == null || demandMinQuality == null) {
            return 0.5;
        }
        Integer s = QUALITY_RANK.get(supplyQuality.toUpperCase());
        Integer d = QUALITY_RANK.get(demandMinQuality.toUpperCase());
        if (s == null || d == null) {
            return 0.5;
        }
        if (s >= d) return 1.0;
        if (s == d - 1) return 0.5;
        return 0.0;
    }

    /**
     * Price score: 1.0 if demand max &gt;= supply expected (overlap), linearly
     * scaled down otherwise. Null defaults to 0.5.
     */
    static double priceScore(BigDecimal supplyExpected, BigDecimal demandMax) {
        if (supplyExpected == null || demandMax == null) {
            return 0.5;
        }
        if (supplyExpected.signum() <= 0 || demandMax.signum() <= 0) {
            return 0.5;
        }
        if (demandMax.compareTo(supplyExpected) >= 0) {
            return 1.0;
        }
        // 5% gap → 0.5; 20%+ gap → 0
        double ratio = demandMax.doubleValue() / supplyExpected.doubleValue();
        return Math.max(0.0, (ratio - 0.80) / 0.20);
    }

    /** Quantity score: 1.0 if supply qty &gt;= demand qty, fractional otherwise. */
    static double quantityScore(BigDecimal supplyQty, BigDecimal demandQty) {
        if (supplyQty == null || demandQty == null) {
            return 0.5;
        }
        if (supplyQty.signum() <= 0 || demandQty.signum() <= 0) {
            return 0.5;
        }
        if (supplyQty.compareTo(demandQty) >= 0) {
            return 1.0;
        }
        return supplyQty.doubleValue() / demandQty.doubleValue();
    }
}
