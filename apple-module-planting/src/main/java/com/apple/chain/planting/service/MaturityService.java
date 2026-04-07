package com.apple.chain.planting.service;

import com.apple.chain.planting.entity.MaturityRecord;
import com.apple.chain.planting.entity.MaturityStandard;
import com.apple.chain.planting.maturity.MaturityRecommendation;

import java.util.List;

/**
 * M6 - Harvest time intelligent recommendation service.
 *
 * <p>Source: design-patches-12-missing.md M6.</p>
 */
public interface MaturityService {

    /**
     * Persist a new maturity sample.
     *
     * <p>The implementation:</p>
     * <ol>
     *   <li>Looks up the {@link MaturityStandard} for the record's variety
     *       (throws {@code BizException} if missing).</li>
     *   <li>Computes weighted score + status via {@code MaturityCalculator}.</li>
     *   <li>Stores the record with score + recommendation populated.</li>
     * </ol>
     *
     * @return the persisted record (with generated id, score, recommendation).
     */
    MaturityRecord recordMeasurement(MaturityRecord record);

    /**
     * Recommend a harvest window for an orchard. Uses the *latest* sample for
     * the orchard. Throws {@code BizException} if no sample exists.
     */
    MaturityRecommendation recommendHarvestWindow(Long orchardId);

    /** Read all defined variety standards (for UI dropdowns). */
    List<MaturityStandard> listStandards();

    /** Read recent maturity samples for an orchard, newest first. */
    List<MaturityRecord> listOrchardRecords(Long orchardId, int limit);
}
