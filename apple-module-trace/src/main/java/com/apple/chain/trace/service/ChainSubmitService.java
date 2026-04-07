package com.apple.chain.trace.service;

import com.apple.chain.trace.entity.ChainRecord;

import java.util.List;
import java.util.Map;

/**
 * M2 service: hand off business snapshots to the chain client and persist
 * the result to {@code tr_chain_record}.
 *
 * The submit path is fire-and-forget: the caller (e.g. M12 trace code generation)
 * gets back a record id in PENDING state and the upload happens asynchronously.
 * Verify and admin retry are synchronous.
 */
public interface ChainSubmitService {

    /**
     * Submit a business snapshot for chain upload.
     * Persists a {@link ChainRecord} in PENDING state and triggers async upload.
     *
     * @param traceCode    the trace code being chained
     * @param businessType BATCH / BOX / FRUIT / cultivation / ...
     * @param businessId   optional business primary key
     * @param snapshot     business state to be hashed and stored on-chain
     * @return the persisted ChainRecord (status=PENDING immediately after submit)
     */
    ChainRecord submit(String traceCode, String businessType, Long businessId, Map<String, Object> snapshot);

    /**
     * Verify a chain record by trace code: re-hash the stored snapshot and
     * confirm it matches the originally uploaded hash. Used by the public
     * verify endpoint.
     *
     * @return verification result map: {valid, hashMatch, chainStatus, chainTxHash, blockHeight}
     */
    Map<String, Object> verifyByTraceCode(String traceCode);

    /** Manual retry for an admin. */
    ChainRecord retry(Long chainRecordId);

    /** List records by status (status=null for all). */
    List<ChainRecord> listByStatus(Integer status, int limit);
}
