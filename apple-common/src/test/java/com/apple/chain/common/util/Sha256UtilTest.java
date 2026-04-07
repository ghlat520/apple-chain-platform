package com.apple.chain.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Sha256Util 单元测试")
class Sha256UtilTest {

    @Test
    @DisplayName("已知 vector: SHA-256(\"abc\")")
    void hashHex_knownVector() {
        // FIPS 180-4 §B.1
        assertThat(Sha256Util.hashHex("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    @DisplayName("空字符串的 SHA-256")
    void hashHex_emptyString() {
        assertThat(Sha256Util.hashHex(""))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    @DisplayName("中文 UTF-8 输入哈希稳定")
    void hashHex_utf8Stable() {
        String r1 = Sha256Util.hashHex("苹果产业链");
        String r2 = Sha256Util.hashHex("苹果产业链");
        assertThat(r1).isEqualTo(r2);
        assertThat(r1).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    @DisplayName("null 输入抛 IllegalArgumentException")
    void hashHex_nullThrows() {
        assertThatThrownBy(() -> Sha256Util.hashHex(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
