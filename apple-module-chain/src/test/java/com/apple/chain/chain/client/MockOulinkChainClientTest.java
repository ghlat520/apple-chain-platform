package com.apple.chain.chain.client;

import com.apple.chain.chain.client.impl.MockOulinkChainClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MockOulinkChainClient 单元测试")
class MockOulinkChainClientTest {

    private MockOulinkChainClient client;

    @BeforeEach
    void setUp() {
        client = new MockOulinkChainClient();
        client.resetForTest();
    }

    @Test
    @DisplayName("uploadHash 成功返回 64 字符 hex tx 与单调递增的 block height")
    void uploadHash_success() {
        ChainUploadResult r1 = client.uploadHash("a".repeat(64), "{\"k\":\"v\"}");
        ChainUploadResult r2 = client.uploadHash("b".repeat(64), "{\"k\":\"v\"}");

        assertThat(r1.success()).isTrue();
        assertThat(r1.txHash()).hasSize(64).matches("[0-9a-f]+");
        assertThat(r1.blockHeight()).isGreaterThanOrEqualTo(1_000_001L);

        assertThat(r2.success()).isTrue();
        assertThat(r2.blockHeight()).isGreaterThan(r1.blockHeight());
    }

    @Test
    @DisplayName("同一 dataHash 两次上传得到不同 txHash（模拟链上语义）")
    void uploadHash_sameInputDifferentTx() {
        String dataHash = "c".repeat(64);
        ChainUploadResult r1 = client.uploadHash(dataHash, "");
        ChainUploadResult r2 = client.uploadHash(dataHash, "");
        assertThat(r1.txHash()).isNotEqualTo(r2.txHash());
    }

    @Test
    @DisplayName("空 dataHash 返回失败")
    void uploadHash_blankInput_fails() {
        assertThat(client.uploadHash(null, "").success()).isFalse();
        assertThat(client.uploadHash("", "").success()).isFalse();
        assertThat(client.uploadHash("   ", "").success()).isFalse();
    }

    @Test
    @DisplayName("MOCK_FAIL 注入触发失败路径")
    void uploadHash_failureInjection() {
        ChainUploadResult r = client.uploadHash("d".repeat(64), "{\"trigger\":\"MOCK_FAIL\"}");
        assertThat(r.success()).isFalse();
        assertThat(r.errorMessage()).contains("mock failure");
        assertThat(r.txHash()).isNull();
    }

    @Test
    @DisplayName("verifyHash 返回原始 dataHash")
    void verifyHash_roundTrip() {
        String dataHash = "e".repeat(64);
        ChainUploadResult r = client.uploadHash(dataHash, "");
        assertThat(client.verifyHash(r.txHash())).isEqualTo(dataHash);
    }

    @Test
    @DisplayName("verifyHash 不存在返回 null")
    void verifyHash_unknown_returnsNull() {
        assertThat(client.verifyHash("not-a-real-hash")).isNull();
        assertThat(client.verifyHash(null)).isNull();
        assertThat(client.verifyHash("")).isNull();
    }
}
