package com.apple.chain.planting;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.planting.dto.OrchardGeoVO;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.mapper.OrchardMapper;
import com.apple.chain.planting.service.impl.OrchardGeoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link OrchardGeoServiceImpl}.
 * Mockito-only, no DB.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrchardGeoService 单元测试")
class OrchardGeoServiceTest {

    @Mock
    private OrchardMapper orchardMapper;

    @InjectMocks
    private OrchardGeoServiceImpl service;

    private static final String VALID_POLYGON = """
            {
              "type": "Polygon",
              "coordinates": [[
                [121.4400, 36.0000],
                [121.4410, 36.0000],
                [121.4410, 36.0010],
                [121.4400, 36.0010],
                [121.4400, 36.0000]
              ]]
            }
            """;

    @Test
    @DisplayName("findInBoundingBox 正常返回 VO 列表 + bbox 自动归一化")
    void findInBoundingBox_normalizesAndMaps() {
        Orchard o = new Orchard();
        o.setId(1L);
        o.setOrchardNo("ORD20260407001");
        o.setOrchardName("烟台果园A");
        o.setVariety("红富士");
        given(orchardMapper.findInBoundingBox(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(List.of(o));

        // Pass coordinates in "wrong" order (lng2<lng1) - service must normalize
        List<OrchardGeoVO> result = service.findInBoundingBox(121.5, 36.1, 121.4, 36.0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrchardName()).isEqualTo("烟台果园A");
        // Verify mapper was called with min/max ordering
        then(orchardMapper).should().findInBoundingBox(121.4, 36.0, 121.5, 36.1);
    }

    @Test
    @DisplayName("saveBoundary 计算质心和面积并持久化")
    void saveBoundary_computesCentroidAndAreaThenUpdates() {
        Orchard o = new Orchard();
        o.setId(1L);
        o.setOrchardName("test");
        given(orchardMapper.selectById(1L)).willReturn(o);
        given(orchardMapper.updateById(any(Orchard.class))).willReturn(1);

        Orchard updated = service.saveBoundary(1L, VALID_POLYGON);

        assertThat(updated.getBoundaryGeojson()).isEqualTo(VALID_POLYGON);
        assertThat(updated.getCenterLat()).isNotNull();
        assertThat(updated.getCenterLng()).isNotNull();
        assertThat(updated.getAreaMu()).isNotNull();
        assertThat(updated.getAreaMu().doubleValue()).isPositive();
        then(orchardMapper).should().updateById(o);
    }

    @Test
    @DisplayName("saveBoundary 果园不存在抛 BizException(NOT_FOUND)")
    void saveBoundary_orchardMissing_throwsNotFound() {
        given(orchardMapper.selectById(99L)).willReturn(null);
        assertThatThrownBy(() -> service.saveBoundary(99L, VALID_POLYGON))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("果园不存在");
    }

    @Test
    @DisplayName("saveBoundary GeoJSON 非法抛 BizException(PARAM_ERROR)")
    void saveBoundary_invalidGeoJson_throwsParamError() {
        Orchard o = new Orchard();
        o.setId(1L);
        given(orchardMapper.selectById(1L)).willReturn(o);

        assertThatThrownBy(() -> service.saveBoundary(1L, "{not json"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("GeoJSON 解析失败");

        // Mapper.updateById should NOT be called when parsing fails
        then(orchardMapper).should(never()).updateById(any(Orchard.class));
    }
}
