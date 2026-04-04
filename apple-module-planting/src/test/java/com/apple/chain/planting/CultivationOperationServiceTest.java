package com.apple.chain.planting;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.planting.entity.CultivationOperation;
import com.apple.chain.planting.mapper.CultivationOperationMapper;
import com.apple.chain.planting.service.impl.CultivationOperationServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for CultivationOperationServiceImpl.
 * Uses Mockito to isolate from DB — no Spring context required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CultivationOperationService 单元测试")
class CultivationOperationServiceTest {

    @Mock
    private CultivationOperationMapper operationMapper;

    @InjectMocks
    private CultivationOperationServiceImpl operationService;

    private CultivationOperation sampleOperation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(operationService, "baseMapper", operationMapper);

        sampleOperation = new CultivationOperation();
        sampleOperation.setId(2001L);
        sampleOperation.setBatchId(1001L);
        sampleOperation.setBatchCode("CB202504010001");
        sampleOperation.setOperationType("FERTILIZE");
        sampleOperation.setOperationDate(LocalDate.of(2024, 3, 15));
        sampleOperation.setOperator("张师傅");
        sampleOperation.setMaterials("{\"name\":\"复合肥\",\"amount\":\"50kg\"}");
        sampleOperation.setRemark("春季施肥");
    }

    // ─── createOperation ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createOperation - 保存成功并返回实体")
    void createOperation_savesAndReturns() {
        given(operationMapper.insert(any(CultivationOperation.class))).willReturn(1);

        CultivationOperation result = operationService.createOperation(sampleOperation);

        assertThat(result).isNotNull();
        assertThat(result.getBatchId()).isEqualTo(1001L);
        assertThat(result.getOperationType()).isEqualTo("FERTILIZE");
        then(operationMapper).should().insert(any(CultivationOperation.class));
    }

    @Test
    @DisplayName("createOperation - 所有操作类型均可创建")
    void createOperation_allOperationTypes_saveSuccessfully() {
        String[] types = {"FERTILIZE", "PESTICIDE", "PRUNE", "IRRIGATE", "HARVEST", "OTHER"};
        given(operationMapper.insert(any(CultivationOperation.class))).willReturn(1);

        for (String type : types) {
            CultivationOperation op = new CultivationOperation();
            op.setBatchId(1001L);
            op.setOperationType(type);
            op.setOperationDate(LocalDate.now());

            CultivationOperation result = operationService.createOperation(op);
            assertThat(result.getOperationType()).isEqualTo(type);
        }
    }

    // ─── updateOperation ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updateOperation - ID不存在时抛出BizException")
    void updateOperation_nonExisting_throwsBizException() {
        given(operationMapper.selectById(99999L)).willReturn(null);

        CultivationOperation update = new CultivationOperation();
        update.setRemark("修改备注");

        assertThatThrownBy(() -> operationService.updateOperation(99999L, update))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("农事操作记录不存在");
    }

    @Test
    @DisplayName("updateOperation - 存在时完成更新并返回最新数据")
    void updateOperation_existing_updatesSuccessfully() {
        CultivationOperation updated = new CultivationOperation();
        updated.setId(2001L);
        updated.setBatchId(1001L);
        updated.setOperationType("PRUNE");
        updated.setRemark("修剪完成");

        given(operationMapper.selectById(2001L))
                .willReturn(sampleOperation)    // first call: existence check
                .willReturn(updated);           // second call: return after update
        given(operationMapper.updateById(any(CultivationOperation.class))).willReturn(1);

        CultivationOperation result = operationService.updateOperation(2001L, new CultivationOperation());

        assertThat(result.getOperationType()).isEqualTo("PRUNE");
        assertThat(result.getRemark()).isEqualTo("修剪完成");
    }

    @Test
    @DisplayName("updateOperation - 更新时ID设置为路径参数ID")
    void updateOperation_setsIdFromPathParam() {
        given(operationMapper.selectById(2001L))
                .willReturn(sampleOperation)
                .willReturn(sampleOperation);
        given(operationMapper.updateById(any(CultivationOperation.class))).willReturn(1);

        CultivationOperation updateReq = new CultivationOperation();
        updateReq.setId(9999L); // different id in body
        operationService.updateOperation(2001L, updateReq);

        // Verify updateById called with id=2001L (from path, not body)
        then(operationMapper).should().updateById((CultivationOperation) argThat(op -> ((CultivationOperation) op).getId().equals(2001L)));
    }

    // ─── deleteOperation ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteOperation - ID不存在时抛出BizException")
    void deleteOperation_nonExisting_throwsBizException() {
        given(operationMapper.deleteById(77777L)).willReturn(0);

        assertThatThrownBy(() -> operationService.deleteOperation(77777L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("农事操作记录不存在");
    }

    @Test
    @DisplayName("deleteOperation - 存在时成功删除")
    void deleteOperation_existing_deletesSuccessfully() {
        given(operationMapper.deleteById(2001L)).willReturn(1);

        assertThatNoException().isThrownBy(() -> operationService.deleteOperation(2001L));
        then(operationMapper).should().deleteById(2001L);
    }

    // ─── listOperations ───────────────────────────────────────────────────────

    @Test
    @DisplayName("listOperations - 无过滤条件时按日期倒序分页")
    void listOperations_noFilter_callsSelectPage() {
        Page<CultivationOperation> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(sampleOperation));
        mockPage.setTotal(1L);

        given(operationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .willReturn(mockPage);

        IPage<CultivationOperation> result = operationService.listOperations(1, 10, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listOperations - 按batchId过滤返回对应批次操作")
    void listOperations_filterByBatchId_returnsOnlyMatchingOps() {
        Page<CultivationOperation> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(sampleOperation));
        mockPage.setTotal(1L);

        given(operationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .willReturn(mockPage);

        IPage<CultivationOperation> result = operationService.listOperations(1, 10, 1001L, null);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getBatchId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("listOperations - 按operationType过滤")
    void listOperations_filterByOperationType_returnsFilteredOps() {
        Page<CultivationOperation> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(sampleOperation));
        mockPage.setTotal(1L);

        given(operationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .willReturn(mockPage);

        IPage<CultivationOperation> result = operationService.listOperations(1, 10, null, "FERTILIZE");

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getOperationType()).isEqualTo("FERTILIZE");
    }

    @Test
    @DisplayName("listOperations - 空白operationType字符串不作为过滤条件")
    void listOperations_blankOperationType_treatedAsNoFilter() {
        Page<CultivationOperation> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(sampleOperation));
        mockPage.setTotal(1L);

        given(operationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .willReturn(mockPage);

        // Empty string should not filter (condition: operationType != null && !isBlank())
        IPage<CultivationOperation> result = operationService.listOperations(1, 10, null, "  ");

        assertThat(result).isNotNull();
    }
}
