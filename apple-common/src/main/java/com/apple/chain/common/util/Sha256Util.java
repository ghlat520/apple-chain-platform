package com.apple.chain.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * SHA-256 helpers. Tiny utility kept here so M2 (chain), M3 (IoT), and M8
 * (payment callback signature) can all share one implementation.
 */
public final class Sha256Util {

    private Sha256Util() {
        // utility
    }

    /**
     * SHA-256 of UTF-8 bytes, returned as 64-char lowercase hex.
     *
     * @throws IllegalArgumentException if input is null
     */
    public static String hashHex(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JDK
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
