package com.apple.chain.planting.maturity;

import com.apple.chain.planting.entity.MaturityRecord;
import com.apple.chain.planting.entity.MaturityStandard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Pure-function maturity scoring + harvest window recommendation.
 *
 * <p>Algorithm (design-patches-12-missing.md M6 §6.4):</p>
 * <pre>
 * score = 0.4 * normalize(brix, std)
 *       + 0.3 * normalize(firmness, std)
 *       + 0.2 * color_match(rgb, std)
 *       + 0.1 * normalize(accumulate_temp, std)
 * score *= 100
 *
 * &lt; 80              -&gt; UNRIPE  (predict N days to optimal via daily temp delta)
 * 80 ..= 95          -&gt; OPTIMAL (window: today .. today + 7)
 * &gt; 95              -&gt; OVERRIPE (window: today .. today, harvest immediately)
 * </pre>
 *
 * <p>This class has no Spring / DB dependencies and is fully unit-testable.</p>
 */
public final class MaturityCalculator {

    /** Lower bound of the optimal window (inclusive). */
    public static final BigDecimal OPTIMAL_LOWER = new BigDecimal("80");
    /** Upper bound of the optimal window (inclusive). */
    public static final BigDecimal OPTIMAL_UPPER = new BigDecimal("95");
    /** Length of recommended optimal window in days. */
    public static final int OPTIMAL_WINDOW_DAYS = 7;

    private static final BigDecimal W_BRIX = new BigDecimal("0.4");
    private static final BigDecimal W_FIRMNESS = new BigDecimal("0.3");
    private static final BigDecimal W_COLOR = new BigDecimal("0.2");
    private static final BigDecimal W_TEMP = new BigDecimal("0.1");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    public static final String STATUS_UNRIPE = "UNRIPE";
    public static final String STATUS_OPTIMAL = "OPTIMAL";
    public static final String STATUS_OVERRIPE = "OVERRIPE";

    private MaturityCalculator() {
        // utility
    }

    /**
     * Compute the 0-100 weighted maturity score for {@code record} against
     * {@code standard}. Pure function — does not mutate inputs.
     */
    public static BigDecimal score(MaturityRecord record, MaturityStandard standard) {
        BigDecimal nBrix = normalizeRange(toBd(record.getBrix()), standard.getBrixMin(), standard.getBrixMax());
        BigDecimal nFirmness = normalizeFirmness(toBd(record.getFirmness()), standard.getFirmnessMin(), standard.getFirmnessMax());
        BigDecimal nColor = colorMatch(record.getColorRgb(), standard.getColorTarget());
        BigDecimal nTemp = normalizeRatio(BigDecimal.valueOf(record.getAccumulateTemp() == null ? 0 : record.getAccumulateTemp()),
                BigDecimal.valueOf(standard.getAccumulateTempTarget() == null ? 0 : standard.getAccumulateTempTarget()));

        BigDecimal weighted = W_BRIX.multiply(nBrix)
                .add(W_FIRMNESS.multiply(nFirmness))
                .add(W_COLOR.multiply(nColor))
                .add(W_TEMP.multiply(nTemp));

        return weighted.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Build a full {@link MaturityRecommendation} from a sample + its standard.
     *
     * @param record   measurement (must have brix, firmness, colorRgb, accumulateTemp)
     * @param standard variety baseline
     * @param today    reference date for window math (use {@code LocalDate.now()} in prod)
     */
    public static MaturityRecommendation recommend(MaturityRecord record,
                                                   MaturityStandard standard,
                                                   LocalDate today) {
        BigDecimal score = score(record, standard);
        BigDecimal confidence = confidence(record, standard);

        if (score.compareTo(OPTIMAL_LOWER) < 0) {
            int days = predictDaysUntilOptimal(record, standard);
            LocalDate start = today.plusDays(days);
            LocalDate end = start.plusDays(OPTIMAL_WINDOW_DAYS);
            return MaturityRecommendation.builder()
                    .status(STATUS_UNRIPE)
                    .score(score)
                    .confidence(confidence)
                    .windowStart(start)
                    .windowEnd(end)
                    .daysUntilOptimal(days)
                    .message("尚未成熟（评分 " + score + "），预计 " + days
                            + " 天后进入最佳采收窗口 " + start + " ~ " + end)
                    .build();
        }

        if (score.compareTo(OPTIMAL_UPPER) <= 0) {
            LocalDate end = today.plusDays(OPTIMAL_WINDOW_DAYS);
            return MaturityRecommendation.builder()
                    .status(STATUS_OPTIMAL)
                    .score(score)
                    .confidence(confidence)
                    .windowStart(today)
                    .windowEnd(end)
                    .daysUntilOptimal(0)
                    .message("已达最佳采收期（评分 " + score + "），建议在 " + today + " ~ " + end + " 内完成采收")
                    .build();
        }

        return MaturityRecommendation.builder()
                .status(STATUS_OVERRIPE)
                .score(score)
                .confidence(confidence)
                .windowStart(today)
                .windowEnd(today)
                .daysUntilOptimal(0)
                .message("已过最佳采收期（评分 " + score + "），请立即采收以避免品质损失")
                .build();
    }

    // ─── normalization helpers ────────────────────────────────────────────────

    /**
     * Map a measurement onto [0, 1] using a "the closer to mid the better" curve
     * for the [{@code min}, {@code max}] range, then bias upward so values inside
     * the range yield ~1, values below ramp up linearly toward {@code min}, and
     * values above start to decay (slight penalty for overshoot).
     */
    public static BigDecimal normalizeRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || min == null || max == null) return ZERO;
        if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
            return ONE;
        }
        if (value.compareTo(min) < 0) {
            // Linear ramp 0..1 from 0 to min
            if (min.compareTo(ZERO) <= 0) return ZERO;
            return clamp01(value.divide(min, 6, RoundingMode.HALF_UP));
        }
        // value > max → over-mature, decay to 0 once value reaches 1.5 * max
        BigDecimal overshoot = value.subtract(max);
        BigDecimal tolerance = max.multiply(new BigDecimal("0.5"));
        if (tolerance.compareTo(ZERO) <= 0) return ZERO;
        BigDecimal decayed = ONE.subtract(overshoot.divide(tolerance, 6, RoundingMode.HALF_UP));
        return clamp01(decayed);
    }

    /**
     * Firmness normalization differs from sugar/temp: lower firmness means
     * riper fruit. Score is highest when firmness is in the standard window;
     * above max ⇒ unripe (linear ramp), below min ⇒ over-soft (penalty).
     */
    public static BigDecimal normalizeFirmness(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null || min == null || max == null) return ZERO;
        if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
            return ONE;
        }
        if (value.compareTo(max) > 0) {
            // Too firm = unripe. Linear ramp 1 → 0 over [max, max + max*0.5]
            BigDecimal overshoot = value.subtract(max);
            BigDecimal tolerance = max.multiply(new BigDecimal("0.5"));
            return clamp01(ONE.subtract(overshoot.divide(tolerance, 6, RoundingMode.HALF_UP)));
        }
        // value < min: over-soft. Drop to 0 within min*0.4
        BigDecimal undershoot = min.subtract(value);
        BigDecimal tolerance = min.multiply(new BigDecimal("0.4"));
        if (tolerance.compareTo(ZERO) <= 0) return ZERO;
        return clamp01(ONE.subtract(undershoot.divide(tolerance, 6, RoundingMode.HALF_UP)));
    }

    /**
     * Ratio normalization for "monotonic increase = better" factors like
     * accumulated temperature. Output is value/target capped at 1.0.
     */
    public static BigDecimal normalizeRatio(BigDecimal value, BigDecimal target) {
        if (value == null || target == null || target.compareTo(ZERO) <= 0) return ZERO;
        BigDecimal r = value.divide(target, 6, RoundingMode.HALF_UP);
        return clamp01(r);
    }

    /**
     * Compare two 6-char hex RGB colors. Score is 1 - (Euclidean distance / max_distance).
     */
    public static BigDecimal colorMatch(String actual, String target) {
        int[] a = parseHex(actual);
        int[] t = parseHex(target);
        if (a == null || t == null) return ZERO;
        long dr = a[0] - t[0];
        long dg = a[1] - t[1];
        long db = a[2] - t[2];
        double dist = Math.sqrt(dr * dr + dg * dg + db * db);
        double maxDist = Math.sqrt(3.0) * 255.0;
        double sim = 1.0 - (dist / maxDist);
        if (sim < 0) sim = 0;
        if (sim > 1) sim = 1;
        return BigDecimal.valueOf(sim).setScale(6, RoundingMode.HALF_UP);
    }

    private static int[] parseHex(String hex) {
        if (hex == null) return null;
        String h = hex.trim();
        if (h.startsWith("#")) h = h.substring(1);
        if (h.length() != 6) return null;
        try {
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            return new int[]{r, g, b};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Confidence: average of normalized factor scores. High when all factors
     * agree, low when one factor (e.g. color) drags the others down.
     */
    public static BigDecimal confidence(MaturityRecord r, MaturityStandard s) {
        BigDecimal nBrix = normalizeRange(toBd(r.getBrix()), s.getBrixMin(), s.getBrixMax());
        BigDecimal nFirm = normalizeFirmness(toBd(r.getFirmness()), s.getFirmnessMin(), s.getFirmnessMax());
        BigDecimal nColor = colorMatch(r.getColorRgb(), s.getColorTarget());
        BigDecimal nTemp = normalizeRatio(BigDecimal.valueOf(r.getAccumulateTemp() == null ? 0 : r.getAccumulateTemp()),
                BigDecimal.valueOf(s.getAccumulateTempTarget() == null ? 0 : s.getAccumulateTempTarget()));
        BigDecimal mean = nBrix.add(nFirm).add(nColor).add(nTemp).divide(new BigDecimal("4"), 4, RoundingMode.HALF_UP);
        // Confidence = 1 - normalized variance from mean (closer agreement = higher confidence)
        BigDecimal var = nBrix.subtract(mean).abs()
                .add(nFirm.subtract(mean).abs())
                .add(nColor.subtract(mean).abs())
                .add(nTemp.subtract(mean).abs())
                .divide(new BigDecimal("4"), 4, RoundingMode.HALF_UP);
        return clamp01(ONE.subtract(var)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Predict how many days until accumulated temperature reaches the target,
     * given the variety's average daily increment. Returns 0 if already met.
     */
    public static int predictDaysUntilOptimal(MaturityRecord r, MaturityStandard s) {
        int current = r.getAccumulateTemp() == null ? 0 : r.getAccumulateTemp();
        int target = s.getAccumulateTempTarget() == null ? 0 : s.getAccumulateTempTarget();
        if (current >= target) return 0;
        BigDecimal daily = s.getDailyTempIncrement();
        if (daily == null || daily.compareTo(ZERO) <= 0) {
            daily = new BigDecimal("18");
        }
        BigDecimal gap = BigDecimal.valueOf(target - current);
        return gap.divide(daily, 0, RoundingMode.CEILING).intValue();
    }

    private static BigDecimal clamp01(BigDecimal v) {
        if (v == null) return ZERO;
        if (v.compareTo(ZERO) < 0) return ZERO;
        if (v.compareTo(ONE) > 0) return ONE;
        return v;
    }

    private static BigDecimal toBd(BigDecimal v) {
        return v == null ? ZERO : v;
    }
}
