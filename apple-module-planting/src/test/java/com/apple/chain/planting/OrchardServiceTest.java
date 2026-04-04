package com.apple.chain.planting;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.planting.entity.OrchardMvp;
import com.apple.chain.planting.mapper.OrchardMvpMapper;
import com.apple.chain.planting.service.impl.OrchardMvpServiceImpl;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for OrchardMvpServiceImpl.
 * Uses Mockito to isolate from DB — no Spring context, no H2 required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrchardMvpService 单元测试")
class OrchardServiceTest {

    @Mock
    private OrchardMvpMapper orchardMapper;

    @InjectMocks
    private OrchardMvpServiceImpl orchardService;

    private OrchardMvp sampleOrchard;

    @BeforeEach
    void setUp() {
        sampleOrchard = new OrchardMvp();
        sampleOrchard.setId(20001L);
        sampleOrchard.setOrchardCode("OC202501010001");
        sampleOrchard.setOrchardName("张家湾苹果园");
        sampleOrchard.setFarmerId(10001L);
        sampleOrchard.setLocation("陕西省延安市洛川县");
        sampleOrchard.setArea(new BigDecimal("120.50"));
        sampleOrchard.setVariety("红富士");
        sampleOrchard.setPlantingYear(2020);
        sampleOrchard.setStatus("ACTIVE");
    }

    @Test
    @DisplayName("getOrchardDetail - 存在时返回果园")
    void getOrchardDetail_existingId_returnsOrchard() {
        given(orchardMapper.selectById(20001L)).willReturn(sampleOrchard);

        OrchardMvp result = orchardService.getOrchardDetail(20001L);

        assertThat(result).isNotNull();
        assertThat(result.getOrchardCode()).isEqualTo("OC202501010001");
        assertThat(result.getOrchardName()).isEqualTo("张家湾苹果园");
        assertThat(result.getVariety()).isEqualTo("红富士");
    }

    @Test
    @DisplayName("getOrchardDetail - 不存在时抛出BizException")
    void getOrchardDetail_nonExistingId_throwsBizException() {
        given(orchardMapper.selectById(99999L)).willReturn(null);

        assertThatThrownBy(() -> orchardService.getOrchardDetail(99999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("果园不存在");
    }

    @Test
    @DisplayName("createOrchard - 自动设置orchardCode和ACTIVE状态")
    void createOrchard_newOrchard_setsCodeAndStatus() {
        OrchardMvp newOrchard = new OrchardMvp();
        newOrchard.setOrchardName("新建测试果园");
        newOrchard.setFarmerId(10001L);

        given(orchardMapper.nextSeq(anyString())).willReturn(1);
        given(orchardMapper.insert(any(OrchardMvp.class))).willReturn(1);

        OrchardMvp result = orchardService.createOrchard(newOrchard);

        assertThat(result.getOrchardCode()).isNotBlank();
        assertThat(result.getOrchardCode()).startsWith("OC");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("createOrchard - orchardCode格式为OC+yyyyMMdd+4位序号")
    void createOrchard_codeFormatIsCorrect() {
        OrchardMvp newOrchard = new OrchardMvp();
        newOrchard.setOrchardName("格式测试果园");
        newOrchard.setFarmerId(10002L);

        given(orchardMapper.nextSeq(anyString())).willReturn(5);
        given(orchardMapper.insert(any(OrchardMvp.class))).willReturn(1);

        OrchardMvp result = orchardService.createOrchard(newOrchard);

        // Code = OC + 8-digit date + 4-digit seq → length must be 2+8+4=14
        assertThat(result.getOrchardCode()).hasSize(14);
        assertThat(result.getOrchardCode()).startsWith("OC");
        assertThat(result.getOrchardCode()).endsWith("0005");
    }

    @Test
    @DisplayName("updateOrchard - 不存在时抛出BizException")
    void updateOrchard_nonExisting_throwsBizException() {
        given(orchardMapper.selectById(88888L)).willReturn(null);

        OrchardMvp update = new OrchardMvp();
        update.setOrchardName("更新失败");

        assertThatThrownBy(() -> orchardService.updateOrchard(88888L, update))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("果园不存在");
    }

    @Test
    @DisplayName("updateOrchard - 存在时完成更新并返回最新数据")
    void updateOrchard_existing_updatesSuccessfully() {
        OrchardMvp existing = sampleOrchard;
        OrchardMvp updated = new OrchardMvp();
        updated.setId(20001L);
        updated.setOrchardCode("OC202501010001");
        updated.setOrchardName("更新后名称");
        updated.setFarmerId(10001L);
        updated.setStatus("INACTIVE");

        given(orchardMapper.selectById(20001L))
                .willReturn(existing)   // first call: existence check
                .willReturn(updated);   // second call: return after update
        given(orchardMapper.updateById(any(OrchardMvp.class))).willReturn(1);

        OrchardMvp result = orchardService.updateOrchard(20001L, new OrchardMvp());

        assertThat(result.getOrchardName()).isEqualTo("更新后名称");
        assertThat(result.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("deleteOrchard - 不存在时抛出BizException")
    void deleteOrchard_nonExisting_throwsBizException() {
        given(orchardMapper.deleteById(77777L)).willReturn(0);

        assertThatThrownBy(() -> orchardService.deleteOrchard(77777L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("果园不存在");
    }

    @Test
    @DisplayName("deleteOrchard - 存在时成功软删除")
    void deleteOrchard_existing_deletesSuccessfully() {
        given(orchardMapper.deleteById(20001L)).willReturn(1);

        assertThatNoException().isThrownBy(() -> orchardService.deleteOrchard(20001L));
        then(orchardMapper).should().deleteById(20001L);
    }
}
