package com.apple.chain.chain.client;

/**
 * Adapter interface for the 奥链 (Aochain) blockchain.
 * <p>
 * Two implementations live behind this interface:
 * <ul>
 *   <li>{@code MockOulinkChainClient} — default for development/CI/staging.
 *       Returns synthetic tx hashes after a small delay; no network I/O.</li>
 *   <li>{@code RealOulinkChainClient} — wraps the actual Aochain SDK.
 *       Will be added when the SDK is delivered by the business partner.</li>
 * </ul>
 * <p>
 * Switching is governed by the property {@code chain.oulink.enabled}:
 * {@code false} (or absent) → mock; {@code true} → real (when present).
 */
public interface OulinkChainClient {

    /**
     * Upload a data hash to the chain together with searchable metadata.
     *
     * @param dataHash  SHA-256 hex of the canonical JSON snapshot
     * @param metadata  small JSON string with searchable fields (traceCode, businessType, ...)
     * @return upload result with on-chain tx hash + block height, or error
     */
    ChainUploadResult uploadHash(String dataHash, String metadata);

    /**
     * Verify an existing on-chain record by its transaction hash.
     *
     * @return the data hash that was originally uploaded, or {@code null} if not found
     */
    String verifyHash(String txHash);
}
