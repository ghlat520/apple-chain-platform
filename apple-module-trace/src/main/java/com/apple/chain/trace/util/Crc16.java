package com.apple.chain.trace.util;

import java.nio.charset.StandardCharsets;

/**
 * CRC-16/CCITT-FALSE implementation.
 * <p>
 * Parameters:
 *   - Polynomial: 0x1021
 *   - Init:       0xFFFF
 *   - RefIn:      false
 *   - RefOut:     false
 *   - XorOut:     0x0000
 * <p>
 * Reference check value: CRC16("123456789") = 0x29B1.
 * <p>
 * Used by M12 three-level traceability code to append a 4-digit hex checksum
 * to BOX and FRUIT codes so consumers can detect tampered or mis-scanned codes
 * without a database round-trip.
 */
public final class Crc16 {

    private static final int POLY = 0x1021;
    private static final int INIT = 0xFFFF;

    private Crc16() {
    }

    /**
     * Compute CRC-16/CCITT-FALSE over the given bytes.
     *
     * @param data input bytes (non-null)
     * @return 16-bit unsigned checksum (0x0000 - 0xFFFF)
     */
    public static int compute(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("CRC16 input must not be null");
        }
        int crc = INIT;
        for (byte b : data) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = ((crc << 1) ^ POLY) & 0xFFFF;
                } else {
                    crc = (crc << 1) & 0xFFFF;
                }
            }
        }
        return crc & 0xFFFF;
    }

    /**
     * Convenience overload for UTF-8 encoded strings.
     */
    public static int compute(String input) {
        if (input == null) {
            throw new IllegalArgumentException("CRC16 input must not be null");
        }
        return compute(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Compute the checksum as a 4-character uppercase hex string (zero-padded).
     */
    public static String hex(String input) {
        return String.format("%04X", compute(input));
    }
}
