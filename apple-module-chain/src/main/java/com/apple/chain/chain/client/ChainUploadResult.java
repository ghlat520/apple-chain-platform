package com.apple.chain.chain.client;

/**
 * Result returned by an {@link OulinkChainClient} upload attempt.
 *
 * @param success      whether the upload succeeded
 * @param txHash       the on-chain transaction hash (null on failure)
 * @param blockHeight  the block at which the tx was mined (null on failure)
 * @param errorMessage human-readable failure cause (null on success)
 */
public record ChainUploadResult(
        boolean success,
        String txHash,
        Long blockHeight,
        String errorMessage
) {

    public static ChainUploadResult ok(String txHash, Long blockHeight) {
        return new ChainUploadResult(true, txHash, blockHeight, null);
    }

    public static ChainUploadResult fail(String error) {
        return new ChainUploadResult(false, null, null, error);
    }
}
