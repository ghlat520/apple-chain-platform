package com.apple.chain.trace;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.trace.entity.TraceBatch;
import com.apple.chain.trace.entity.TraceCode;
import com.apple.chain.trace.mapper.TraceBatchMapper;
import com.apple.chain.trace.mapper.TraceCodeMapper;
import com.apple.chain.trace.service.impl.TraceCodeServiceImpl;
import com.apple.chain.trace.util.Crc16;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for TraceCodeServiceImpl (Mockito — no Spring, no DB).
 * Exercises the pure-logic portions: code format, CRC suffix, verify flow.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TraceCodeService 单元测试")
class TraceCodeServiceTest {

    @Mock
    private TraceBatchMapper traceBatchMapper;

    @Mock
    private TraceCodeMapper traceCodeMapper;

    @InjectMocks
    private TraceCodeServiceImpl service;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl has a generic `baseMapper` field that Mockito
        // can't resolve via type erasure — wire it manually.
        ReflectionTestUtils.setField(service, "baseMapper", traceCodeMapper);
    }

    // ---- Pure formatter tests ---------------------------------------------

    @Test
    @DisplayName("BOX 编码格式: {batchCode}-B{seq3}-{CRC4}")
    void boxCodeFormat() {
        String code = TraceCodeServiceImpl.buildBoxCode("TB202604070001", 1);
        assertThat(code).startsWith("TB202604070001-B001-");
        assertThat(code).hasSize("TB202604070001-B001-".length() + 4);
        String payload = code.substring(0, code.lastIndexOf('-'));
        String crc = code.substring(code.lastIndexOf('-') + 1);
        assertThat(crc).isEqualTo(Crc16.hex(payload));
    }

    @Test
    @DisplayName("FRUIT 编码格式: {boxCode}-F{seq4}-{CRC4}")
    void fruitCodeFormat() {
        String boxCode = "TB202604070001-B001-ABCD";
        String code = TraceCodeServiceImpl.buildFruitCode(boxCode, 42);
        assertThat(code).startsWith(boxCode + "-F0042-");
        String payload = code.substring(0, code.lastIndexOf('-'));
        String crc = code.substring(code.lastIndexOf('-') + 1);
        assertThat(crc).isEqualTo(Crc16.hex(payload));
    }

    @Test
    @DisplayName("序号补零: BOX 第 7 号 → -B007-, FRUIT 第 123 号 → -F0123-")
    void zeroPadding() {
        assertThat(TraceCodeServiceImpl.buildBoxCode("TB202604070001", 7))
                .contains("-B007-");
        assertThat(TraceCodeServiceImpl.buildFruitCode("TB202604070001-B001-ABCD", 123))
                .contains("-F0123-");
    }

    // ---- Param validation -------------------------------------------------

    @Test
    @DisplayName("generateBoxCodes: batchId 为空应抛 BizException")
    void generateBox_nullBatchId() {
        assertThatThrownBy(() -> service.generateBoxCodes(null, 10))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("generateBoxCodes: boxCount 非法应抛 BizException")
    void generateBox_invalidCount() {
        assertThatThrownBy(() -> service.generateBoxCodes(1L, 0))
                .isInstanceOf(BizException.class);
        assertThatThrownBy(() -> service.generateBoxCodes(1L, 20000))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("generateBoxCodes: batch 不存在应抛 BizException")
    void generateBox_batchNotFound() {
        given(traceBatchMapper.selectById(99L)).willReturn(null);
        assertThatThrownBy(() -> service.generateBoxCodes(99L, 1))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("generateFruitCodes: box 不存在应抛 BizException")
    void generateFruit_boxNotFound() {
        given(traceCodeMapper.findByCode("NOPE")).willReturn(null);
        assertThatThrownBy(() -> service.generateFruitCodes("NOPE", 10))
                .isInstanceOf(BizException.class);
    }

    // ---- verifyCode -------------------------------------------------------

    @Test
    @DisplayName("verifyCode: null / 空 → valid=false")
    void verify_null() {
        Map<String, Object> r = service.verifyCode(null);
        assertThat(r.get("valid")).isEqualTo(false);
    }

    @Test
    @DisplayName("verifyCode: 合法 BATCH 编码 → 走 trace_batch 查询")
    void verify_batchCode() {
        TraceBatch batch = new TraceBatch();
        batch.setId(100L);
        batch.setBatchCode("TB202604070001");
        batch.setStatus("CREATED");
        given(traceBatchMapper.findByBatchCode("TB202604070001")).willReturn(batch);

        Map<String, Object> r = service.verifyCode("TB202604070001");
        assertThat(r.get("valid")).isEqualTo(true);
        assertThat(r.get("granularity")).isEqualTo("BATCH");
        assertThat(r.get("batchId")).isEqualTo(100L);
    }

    @Test
    @DisplayName("verifyCode: BATCH 编码但不存在 → valid=false")
    void verify_batchNotFound() {
        given(traceBatchMapper.findByBatchCode(anyString())).willReturn(null);
        Map<String, Object> r = service.verifyCode("TB202604070099");
        assertThat(r.get("valid")).isEqualTo(false);
    }

    @Test
    @DisplayName("verifyCode: CRC 不匹配 → valid=false, 不查库")
    void verify_crcMismatch() {
        // Legit BOX code shape but with a tampered CRC suffix
        String bad = "TB202604070001-B001-0000";
        Map<String, Object> r = service.verifyCode(bad);
        assertThat(r.get("valid")).isEqualTo(false);
        assertThat(r.get("message")).isEqualTo("CRC 校验失败");
    }

    @Test
    @DisplayName("verifyCode: CRC 正确但库里查不到 → valid=false, 编码未登记")
    void verify_crcOkButNotRegistered() {
        String boxCode = TraceCodeServiceImpl.buildBoxCode("TB202604070001", 1);
        given(traceCodeMapper.findByCode(boxCode)).willReturn(null);
        Map<String, Object> r = service.verifyCode(boxCode);
        assertThat(r.get("valid")).isEqualTo(false);
        assertThat(r.get("message")).isEqualTo("编码未登记");
    }

    @Test
    @DisplayName("verifyCode: CRC + DB 都通过 → valid=true 并回填 granularity/status")
    void verify_ok() {
        String boxCode = TraceCodeServiceImpl.buildBoxCode("TB202604070001", 1);
        TraceCode tc = new TraceCode();
        tc.setCode(boxCode);
        tc.setGranularity("BOX");
        tc.setStatus("ACTIVE");
        tc.setBatchId(100L);
        tc.setParentCode("TB202604070001");
        given(traceCodeMapper.findByCode(boxCode)).willReturn(tc);

        Map<String, Object> r = service.verifyCode(boxCode);
        assertThat(r.get("valid")).isEqualTo(true);
        assertThat(r.get("granularity")).isEqualTo("BOX");
        assertThat(r.get("status")).isEqualTo("ACTIVE");
        assertThat(r.get("parentCode")).isEqualTo("TB202604070001");
    }
}
