package com.apple.chain.trace;

import com.apple.chain.trace.util.Crc16;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CRC-16/CCITT-FALSE reference vectors.
 * <p>
 * These values are taken from well-known references so any regression in the
 * implementation (wrong poly, wrong init, reflect flags) will be caught.
 *   - "123456789"           → 0x29B1   (the canonical CRC-16 check value)
 *   - ""                    → 0xFFFF   (init value, no input)
 *   - "A"                   → 0xB915
 *   - "ABC"                 → 0xF508
 *   - "Hello, World!"       → 0x67DA
 *   - "TB20260407-B001"     → deterministic — asserted as 4-hex stable value
 */
@DisplayName("CRC-16/CCITT-FALSE 标准向量测试")
class Crc16Test {

    @Test
    @DisplayName("标准向量: 123456789 → 0x29B1")
    void checkValue() {
        assertThat(Crc16.compute("123456789")).isEqualTo(0x29B1);
        assertThat(Crc16.hex("123456789")).isEqualTo("29B1");
    }

    @Test
    @DisplayName("空字符串 → 初始值 0xFFFF")
    void emptyInput() {
        assertThat(Crc16.compute("")).isEqualTo(0xFFFF);
        assertThat(Crc16.hex("")).isEqualTo("FFFF");
    }

    @Test
    @DisplayName("单字符 A → 0xB915")
    void singleChar() {
        assertThat(Crc16.compute("A")).isEqualTo(0xB915);
    }

    @Test
    @DisplayName("ABC → 0xF508")
    void threeChars() {
        assertThat(Crc16.compute("ABC")).isEqualTo(0xF508);
    }

    @Test
    @DisplayName("Hello, World! → 0x67DA")
    void helloWorld() {
        assertThat(Crc16.compute("Hello, World!")).isEqualTo(0x67DA);
    }

    @Test
    @DisplayName("业务 payload: TB20260407-B001 — 计算结果必须稳定且可反向验证")
    void businessPayload() {
        String payload = "TB20260407-B001";
        int v1 = Crc16.compute(payload);
        int v2 = Crc16.compute(payload);
        assertThat(v1).isEqualTo(v2);                      // deterministic
        assertThat(Crc16.hex(payload)).hasSize(4);         // always 4 hex chars
        assertThat(Crc16.hex(payload)).isUpperCase();
        // 4-hex representation matches numeric value
        assertThat(Integer.parseInt(Crc16.hex(payload), 16)).isEqualTo(v1);
    }

    @Test
    @DisplayName("null 输入必须抛 IllegalArgumentException")
    void nullInput() {
        assertThatThrownBy(() -> Crc16.compute((String) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Crc16.compute((byte[]) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
