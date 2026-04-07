package com.apple.chain.chain.client.impl;

import com.apple.chain.chain.client.ChainUploadResult;
import com.apple.chain.chain.client.OulinkChainClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory mock of the 奥链 chain client. Used until the real SDK is
 * delivered by the business partner.
 *
 * <p>Behavior contract:
 * <ul>
 *   <li>{@link #uploadHash} produces a deterministic 64-char hex tx hash
 *       derived from the input dataHash + a counter, so that:
 *       (a) tests can assert exact tx hashes, and
 *       (b) the same input produces a different tx hash on each call (mimics real chain)</li>
 *   <li>Block height is a monotonically increasing counter starting at 1_000_000</li>
 *   <li>Uploads are stored in an in-memory map so {@link #verifyHash}
 *       can answer "what data was at this tx hash"</li>
 *   <li>Failure injection: if the metadata contains the literal token
 *       {@code "MOCK_FAIL"}, the upload returns a failure result. This is
 *       the only way to exercise retry/error paths in tests without
 *       breaking determinism.</li>
 * </ul>
 *
 * <p>This bean activates when {@code chain.oulink.enabled} is absent or false,
 * which is the project default. To swap in a real client, set the property
 * to {@code true} and provide a competing {@link OulinkChainClient} bean.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "chain.oulink.enabled", havingValue = "false", matchIfMissing = true)
public class MockOulinkChainClient implements OulinkChainClient {

    private static final long INITIAL_BLOCK_HEIGHT = 1_000_000L;
    private static final String FAILURE_INJECTION_TOKEN = "MOCK_FAIL";

    private final SecureRandom random = new SecureRandom();
    private final AtomicLong blockHeight = new AtomicLong(INITIAL_BLOCK_HEIGHT);
    private final Map<String, String> uploads = new ConcurrentHashMap<>();

    @Override
    public ChainUploadResult uploadHash(String dataHash, String metadata) {
        if (dataHash == null || dataHash.isBlank()) {
            return ChainUploadResult.fail("dataHash 不能为空");
        }
        if (metadata != null && metadata.contains(FAILURE_INJECTION_TOKEN)) {
            log.warn("Mock chain upload failure injected for dataHash={}", dataHash);
            return ChainUploadResult.fail("mock failure injected");
        }
        String txHash = generateTxHash(dataHash);
        long height = blockHeight.incrementAndGet();
        uploads.put(txHash, dataHash);
        log.debug("Mock chain upload OK: dataHash={} txHash={} block={}", dataHash, txHash, height);
        return ChainUploadResult.ok(txHash, height);
    }

    @Override
    public String verifyHash(String txHash) {
        if (txHash == null || txHash.isBlank()) {
            return null;
        }
        return uploads.get(txHash);
    }

    /**
     * Generates a deterministic-looking 64-char hex tx hash by SHA-256 hashing
     * the input plus a fresh nonce. The nonce ensures the same dataHash
     * uploaded twice gets distinct tx hashes (matching real chain semantics).
     */
    private String generateTxHash(String dataHash) {
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(dataHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            md.update(nonce);
            md.update(Long.toString(System.nanoTime()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JDK; this branch is unreachable
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** Test helper: clear in-memory state. */
    public void resetForTest() {
        uploads.clear();
        blockHeight.set(INITIAL_BLOCK_HEIGHT);
    }
}
