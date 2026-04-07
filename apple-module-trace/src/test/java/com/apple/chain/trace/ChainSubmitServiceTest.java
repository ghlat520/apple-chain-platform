package com.apple.chain.trace;

import com.apple.chain.chain.client.ChainUploadResult;
import com.apple.chain.chain.client.OulinkChainClient;
import com.apple.chain.common.exception.BizException;
import com.apple.chain.trace.entity.ChainRecord;
import com.apple.chain.trace.mapper.ChainRecordMapper;
import com.apple.chain.trace.service.impl.ChainSubmitServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for ChainSubmitServiceImpl.
 * Mockito-only — no Spring context, no async path. The async upload listener
 * is exercised separately in integration tests; here we verify the synchronous
 * surface (submit, verify, retry, doUpload via retry).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChainSubmitService 单元测试")
class ChainSubmitServiceTest {

    @Mock
    private ChainRecordMapper chainRecordMapper;

    @Mock
    private OulinkChainClient chainClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ChainSubmitServiceImpl service;

    private Map<String, Object> sampleSnapshot() {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("traceCode", "TB202604070001");
        snap.put("granularity", "BATCH");
        snap.put("orchardId", 100L);
        return snap;
    }

    @Test
    @DisplayName("submit 持久化 PENDING 记录并发布事件")
    void submit_persistsAndPublishesEvent() {
        ChainRecord record = service.submit("TB202604070001", "BATCH", 1L, sampleSnapshot());

        assertThat(record).isNotNull();
        assertThat(record.getTraceCode()).isEqualTo("TB202604070001");
        assertThat(record.getBusinessType()).isEqualTo("BATCH");
        assertThat(record.getDataHash()).hasSize(64);
        assertThat(record.getDataSnapshot()).contains("TB202604070001");
        assertThat(record.getChainStatus()).isEqualTo(ChainRecord.STATUS_PENDING);
        assertThat(record.getRetryCount()).isZero();

        then(chainRecordMapper).should().insert(any(ChainRecord.class));
        then(eventPublisher).should().publishEvent(any(ChainSubmitServiceImpl.ChainUploadRequestedEvent.class));
    }

    @Test
    @DisplayName("submit 相同 snapshot 哈希稳定（key 顺序不影响）")
    void submit_canonicalHashStable() {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("z", 1);
        a.put("a", 2);
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("a", 2);
        b.put("z", 1);

        ChainRecord r1 = service.submit("T1", "BATCH", null, a);
        ChainRecord r2 = service.submit("T1", "BATCH", null, b);

        assertThat(r1.getDataHash()).isEqualTo(r2.getDataHash());
    }

    @Test
    @DisplayName("submit 参数校验：空 traceCode 抛 BizException")
    void submit_blankTraceCode_throws() {
        assertThatThrownBy(() -> service.submit("", "BATCH", null, sampleSnapshot()))
                .isInstanceOf(BizException.class);
        assertThatThrownBy(() -> service.submit("T", "", null, sampleSnapshot()))
                .isInstanceOf(BizException.class);
        assertThatThrownBy(() -> service.submit("T", "BATCH", null, null))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("retry 成功路径：调链上传 → 更新为 SUCCESS")
    void retry_successPath() {
        ChainRecord existing = new ChainRecord();
        existing.setId(99L);
        existing.setTraceCode("T1");
        existing.setBusinessType("BATCH");
        existing.setDataHash("h".repeat(64));
        existing.setDataSnapshot("{\"k\":\"v\"}");
        existing.setChainStatus(ChainRecord.STATUS_RETRY);
        existing.setRetryCount(1);
        given(chainRecordMapper.selectById(99L)).willReturn(existing);
        given(chainClient.uploadHash(eq("h".repeat(64)), anyString()))
                .willReturn(ChainUploadResult.ok("tx-abc", 1234L));

        ChainRecord result = service.retry(99L);

        assertThat(result.getChainStatus()).isEqualTo(ChainRecord.STATUS_SUCCESS);
        assertThat(result.getChainTxHash()).isEqualTo("tx-abc");
        assertThat(result.getChainBlockHeight()).isEqualTo(1234L);
        assertThat(result.getRetryCount()).isEqualTo(2); // bumped before doUpload
        then(chainRecordMapper).should().updateById(any(ChainRecord.class));
    }

    @Test
    @DisplayName("retry 失败到达 MAX_RETRY 设为 FAILED")
    void retry_exhaustsRetries() {
        ChainRecord existing = new ChainRecord();
        existing.setId(99L);
        existing.setTraceCode("T1");
        existing.setBusinessType("BATCH");
        existing.setDataHash("h".repeat(64));
        existing.setDataSnapshot("{\"k\":\"v\"}");
        existing.setChainStatus(ChainRecord.STATUS_RETRY);
        existing.setRetryCount(ChainRecord.MAX_RETRY); // already at max BEFORE retry bumps it
        given(chainRecordMapper.selectById(99L)).willReturn(existing);
        given(chainClient.uploadHash(anyString(), anyString()))
                .willReturn(ChainUploadResult.fail("network down"));

        ChainRecord result = service.retry(99L);

        assertThat(result.getChainStatus()).isEqualTo(ChainRecord.STATUS_FAILED);
        assertThat(result.getErrorMsg()).contains("network down");
    }

    @Test
    @DisplayName("retry 已成功的记录直接返回（幂等）")
    void retry_alreadySuccess_isNoop() {
        ChainRecord existing = new ChainRecord();
        existing.setId(99L);
        existing.setChainStatus(ChainRecord.STATUS_SUCCESS);
        existing.setChainTxHash("tx-old");
        given(chainRecordMapper.selectById(99L)).willReturn(existing);

        ChainRecord result = service.retry(99L);

        assertThat(result.getChainTxHash()).isEqualTo("tx-old");
        then(chainClient).should(never()).uploadHash(anyString(), anyString());
    }

    @Test
    @DisplayName("retry 不存在抛 NOT_FOUND")
    void retry_notFound() {
        given(chainRecordMapper.selectById(404L)).willReturn(null);
        assertThatThrownBy(() -> service.retry(404L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("verifyByTraceCode 哈希一致 + SUCCESS → valid=true")
    void verify_validHashAndSuccess() {
        ChainRecord record = new ChainRecord();
        record.setTraceCode("T1");
        // canonical JSON of {"a":1}
        String snapshot = "{\"a\":1}";
        record.setDataSnapshot(snapshot);
        record.setDataHash(com.apple.chain.common.util.Sha256Util.hashHex(snapshot));
        record.setChainStatus(ChainRecord.STATUS_SUCCESS);
        record.setChainTxHash("tx-abc");
        record.setChainBlockHeight(1000L);
        given(chainRecordMapper.findLatestByTraceCode("T1")).willReturn(record);

        Map<String, Object> result = service.verifyByTraceCode("T1");

        assertThat(result.get("valid")).isEqualTo(true);
        assertThat(result.get("hashMatch")).isEqualTo(true);
        assertThat(result.get("chainTxHash")).isEqualTo("tx-abc");
    }

    @Test
    @DisplayName("verifyByTraceCode 哈希被篡改 → hashMatch=false, valid=false")
    void verify_tamperDetected() {
        ChainRecord record = new ChainRecord();
        record.setTraceCode("T1");
        record.setDataSnapshot("{\"a\":1}");
        record.setDataHash("0".repeat(64)); // wrong hash → tamper signal
        record.setChainStatus(ChainRecord.STATUS_SUCCESS);
        given(chainRecordMapper.findLatestByTraceCode("T1")).willReturn(record);

        Map<String, Object> result = service.verifyByTraceCode("T1");

        assertThat(result.get("valid")).isEqualTo(false);
        assertThat(result.get("hashMatch")).isEqualTo(false);
    }

    @Test
    @DisplayName("verifyByTraceCode 不存在")
    void verify_notFound() {
        given(chainRecordMapper.findLatestByTraceCode("X")).willReturn(null);
        Map<String, Object> result = service.verifyByTraceCode("X");
        assertThat(result.get("valid")).isEqualTo(false);
        assertThat(result.get("message")).isEqualTo("未找到上链记录");
    }

    @Test
    @DisplayName("listByStatus 限制 limit 在 [1,500]")
    void listByStatus_clampsLimit() {
        given(chainRecordMapper.findByStatus(eq(0), eq(1))).willReturn(java.util.List.of());
        given(chainRecordMapper.findByStatus(eq(0), eq(500))).willReturn(java.util.List.of());

        service.listByStatus(0, 0);
        service.listByStatus(0, 9999);

        then(chainRecordMapper).should().findByStatus(0, 1);
        then(chainRecordMapper).should().findByStatus(0, 500);
    }
}
