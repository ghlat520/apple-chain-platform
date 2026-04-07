package com.apple.chain.planting.maturity;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable result of the harvest-window recommendation algorithm.
 *
 * <p>Returned by {@code MaturityService#recommendHarvestWindow}.</p>
 *
 * <ul>
 *   <li>{@link #status} - one of UNRIPE / OPTIMAL / OVERRIPE.</li>
 *   <li>{@link #score}  - 0-100 weighted maturity score.</li>
 *   <li>{@link #confidence} - 0-1 confidence based on factor agreement.</li>
 *   <li>{@link #windowStart}/{@link #windowEnd} - recommended 7-day window.
 *       For UNRIPE the window starts at the predicted ETA day.</li>
 *   <li>{@link #message} - human readable Chinese explanation.</li>
 * </ul>
 */
@Getter
@Builder
public class MaturityRecommendation {

    private final String status;
    private final BigDecimal score;
    private final BigDecimal confidence;
    private final LocalDate windowStart;
    private final LocalDate windowEnd;
    private final Integer daysUntilOptimal;
    private final String message;
}
