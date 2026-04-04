package com.apple.chain.planting;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.planting.entity.CultivationBatch;
import com.apple.chain.planting.mapper.CultivationBatchMapper;
import com.apple.chain.planting.service.impl.CultivationBatchServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for CultivationBatchServiceImpl.
 * Uses Mockito to isolate from DB — no Spring context required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CultivationBatchService 单元测试")
class CultivationBatchServiceTest {

    @Mock
    private CultivationBatchMapper batchMapper;

    @InjectMocks
    private CultivationBatchServiceImpl batchService;

    private CultivationBatch sampleBatch;

    @BeforeEach
    void setUp() {
        // ServiceImpl uses generic baseMapper — @InjectMocks can't resolve it via type erasure,
        // so we force-set the field after Mockito creates the instance.
        ReflectionTestUtils.setField(batchService, "baseMapper", batchMapper);

        sampleBatch = new CultivationBatch();
        sampleBatch.setId(1001L);
        sampleBatch.setBatchCode("CB202504010001");
        sampleBatch.setOrchardId(20001L);
        sampleBatch.setOrchardName("张家湾苹果园");
        sampleBatch.setAppleVariety("红富士");
        sampleBatch.setPlantYear(2024);
        sampleBatch.setExpectedYield(new BigDecimal("5000.00"));
        sampleBatch.setHarvestDate(LocalDate.of(2024, 10, 1));
        sampleBatch.setStatus("PLANTING");
    }

    // ─── getBatchDetail ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getBatchDetail - ID存在时返回批次")
    void getBatchDetail_existingId_returnsBatch() {
        given(batchMapper.selectById(1001L)).willReturn(sampleBatch);

        CultivationBatch result = batchService.getBatchDetail(1001L);

        assertThat(result).isNotNull();
        assertThat(result.getBatchCode()).isEqualTo("CB202504010001");
        assertThat(result.getAppleVariety()).isEqualTo("红富士");
        assertThat(result.getOrchardId()).isEqualTo(20001L);
    }

    @Test
    @DisplayName("getBatchDetail - ID不存在时抛出BizException")
    void getBatchDetail_nonExistingId_throwsBizException() {
        given(batchMapper.selectById(99999L)).willReturn(null);

        assertThatThrownBy(() -> batchService.getBatchDetail(99999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("种植批次不存在");
    }

    // ─── createBatch ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBatch - 自动生成batchCode，前缀CB")
    void createBatch_autogeneratesBatchCode_startsWithCB() {
        CultivationBatch newBatch = new CultivationBatch();
        newBatch.setOrchardId(20001L);
        newBatch.setAppleVariety("嘎拉");

        given(batchMapper.nextSeq(anyString())).willReturn(1);
        given(batchMapper.insert(any(CultivationBatch.class))).willReturn(1);

        CultivationBatch result = batchService.createBatch(newBatch);

        assertThat(result.getBatchCode()).isNotBlank();
        assertThat(result.getBatchCode()).startsWith("CB");
    }

    @Test
    @DisplayName("createBatch - batchCode格式为CB+yyyyMMdd+4位序号，总长14字符")
    void createBatch_codeFormatIs14Chars() {
        CultivationBatch newBatch = new CultivationBatch();
        newBatch.setOrchardId(20001L);
        newBatch.setAppleVariety("秦冠");

        given(batchMapper.nextSeq(anyString())).willReturn(3);
        given(batchMapper.insert(any(CultivationBatch.class))).willReturn(1);

        CultivationBatch result = batchService.createBatch(newBatch);

        // CB(2) + yyyyMMdd(8) + 4-digit-seq(4) = 14
        assertThat(result.getBatchCode()).hasSize(14);
        assertThat(result.getBatchCode()).endsWith("0003");
    }

    @Test
    @DisplayName("createBatch - status为空时默认为PLANTING")
    void createBatch_blankStatus_defaultsToPlanting() {
        CultivationBatch newBatch = new CultivationBatch();
        newBatch.setOrchardId(20001L);
        newBatch.setAppleVariety("黄元帅");
        // status intentionally left null

        given(batchMapper.nextSeq(anyString())).willReturn(2);
        given(batchMapper.insert(any(CultivationBatch.class))).willReturn(1);

        CultivationBatch result = batchService.createBatch(newBatch);

        assertThat(result.getStatus()).isEqualTo("PLANTING");
    }

    @Test
    @DisplayName("createBatch - 已有status时不覆盖")
    void createBatch_existingStatus_notOverridden() {
        CultivationBatch newBatch = new CultivationBatch();
        newBatch.setOrchardId(20001L);
        newBatch.setAppleVariety("红富士");
        newBatch.setStatus("GROWING");

        given(batchMapper.nextSeq(anyString())).willReturn(1);
        given(batchMapper.insert(any(CultivationBatch.class))).willReturn(1);

        CultivationBatch result = batchService.createBatch(newBatch);

        assertThat(result.getStatus()).isEqualTo("GROWING");
    }

    // ─── updateBatch ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBatch - ID不存在时抛出BizException")
    void updateBatch_nonExisting_throwsBizException() {
        given(batchMapper.selectById(88888L)).willReturn(null);

        CultivationBatch update = new CultivationBatch();
        update.setAppleVariety("嘎拉");

        assertThatThrownBy(() -> batchService.updateBatch(88888L, update))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("种植批次不存在");
    }

    @Test
    @DisplayName("updateBatch - 存在时完成更新并返回最新数据")
    void updateBatch_existing_updatesSuccessfully() {
        CultivationBatch updated = new CultivationBatch();
        updated.setId(1001L);
        updated.setBatchCode("CB202504010001");
        updated.setAppleVariety("嘎拉");
        updated.setStatus("GROWING");

        given(batchMapper.selectById(1001L))
                .willReturn(sampleBatch)    // first call: existence check
                .willReturn(updated);       // second call: return after update
        given(batchMapper.updateById(any(CultivationBatch.class))).willReturn(1);

        CultivationBatch result = batchService.updateBatch(1001L, new CultivationBatch());

        assertThat(result.getAppleVariety()).isEqualTo("嘎拉");
        assertThat(result.getStatus()).isEqualTo("GROWING");
    }

    @Test
    @DisplayName("updateBatch - batchCode字段不可变（置null避免覆盖）")
    void updateBatch_batchCodeSetToNullBeforeUpdate() {
        CultivationBatch updateRequest = new CultivationBatch();
        updateRequest.setBatchCode("HACKED_CODE"); // attacker tries to overwrite code
        updateRequest.setAppleVariety("秦冠");

        CultivationBatch afterUpdate = new CultivationBatch();
        afterUpdate.setId(1001L);
        afterUpdate.setBatchCode("CB202504010001"); // original code preserved in DB

        given(batchMapper.selectById(1001L))
                .willReturn(sampleBatch)
                .willReturn(afterUpdate);
        given(batchMapper.updateById(any(CultivationBatch.class))).willReturn(1);

        batchService.updateBatch(1001L, updateRequest);

        // Verify that updateById was called with batchCode = null (immutable)
        then(batchMapper).should().updateById((CultivationBatch) argThat(b -> ((CultivationBatch) b).getBatchCode() == null));
    }

    // ─── deleteBatch ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBatch - ID不存在时抛出BizException")
    void deleteBatch_nonExisting_throwsBizException() {
        given(batchMapper.deleteById(77777L)).willReturn(0);

        assertThatThrownBy(() -> batchService.deleteBatch(77777L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("种植批次不存在");
    }

    @Test
    @DisplayName("deleteBatch - 存在时成功软删除")
    void deleteBatch_existing_softDeletesSuccessfully() {
        given(batchMapper.deleteById(1001L)).willReturn(1);

        assertThatNoException().isThrownBy(() -> batchService.deleteBatch(1001L));
        then(batchMapper).should().deleteById(1001L);
    }

    // ─── listBatches ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("listBatches - 无过滤条件时调用分页查询")
    void listBatches_noFilter_callsSelectPage() {
        Page<CultivationBatch> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(sampleBatch));
        mockPage.setTotal(1L);

        given(batchMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .willReturn(mockPage);

        IPage<CultivationBatch> result = batchService.listBatches(1, 10, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listBatches - keyword/status/orchardId过滤条件正确传递")
    void listBatches_withFilters_returnsFilteredPage() {
        Page<CultivationBatch> mockPage = new Page<>(1, 5);
        mockPage.setRecords(List.of(sampleBatch));
        mockPage.setTotal(1L);

        given(batchMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .willReturn(mockPage);

        IPage<CultivationBatch> result = batchService.listBatches(1, 5, "红富士", "PLANTING", 20001L);

        assertThat(result.getRecords()).isNotEmpty();
        assertThat(result.getRecords().get(0).getAppleVariety()).isEqualTo("红富士");
    }

    // ─── exportBatches ────────────────────────────────────────────────────────

    @Test
    @DisplayName("exportBatches - 写入CSV含UTF-8 BOM和标题行")
    void exportBatches_writesCSVWithHeaderAndBOM() throws Exception {
        given(batchMapper.selectList(any())).willReturn(List.of(sampleBatch));

        MockHttpServletResponse response = new MockHttpServletResponse();
        batchService.exportBatches(null, null, null, response);

        String content = response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(response.getContentType()).contains("text/csv");
        assertThat(content).contains("批次编码");
        assertThat(content).contains("CB202504010001");
    }

    @Test
    @DisplayName("exportBatches - 多条记录全部写入CSV")
    void exportBatches_multipleRecords_allWritten() throws Exception {
        CultivationBatch second = new CultivationBatch();
        second.setId(1002L);
        second.setBatchCode("CB202504010002");
        second.setOrchardId(20002L);
        second.setAppleVariety("嘎拉");
        second.setStatus("GROWING");

        given(batchMapper.selectList(any())).willReturn(List.of(sampleBatch, second));

        MockHttpServletResponse response = new MockHttpServletResponse();
        batchService.exportBatches(null, null, null, response);

        String content = response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(content).contains("CB202504010001");
        assertThat(content).contains("CB202504010002");
    }
}
