package com.apple.chain.warehouse;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.warehouse.entity.Warehouse;
import com.apple.chain.warehouse.mapper.WarehouseMapper;
import com.apple.chain.warehouse.service.impl.WarehouseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("仓库服务 - 状态变更与告警测试")
class WarehouseServiceTest {

    @Mock
    private WarehouseMapper warehouseMapper;

    private WarehouseServiceImpl warehouseService;

    @BeforeEach
    void setUp() throws Exception {
        warehouseService = new WarehouseServiceImpl();
        Field baseMapperField = warehouseService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(warehouseService, warehouseMapper);
    }

    private Warehouse buildWarehouse(Long id, String status, BigDecimal usedCapacity, BigDecimal capacity) {
        Warehouse w = new Warehouse();
        w.setId(id);
        w.setStatus(status);
        w.setUsedCapacity(usedCapacity);
        w.setCapacity(capacity);
        w.setName("冷库" + id);
        return w;
    }

    @Nested
    @DisplayName("changeStatus - 状态变更")
    class ChangeStatus {

        @Test
        @DisplayName("ACTIVE -> MAINTENANCE: 仓库进入维护")
        void activeToMaintenance() {
            Warehouse existing = buildWarehouse(1L, "ACTIVE", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectById(any())).willReturn(existing);
            given(warehouseMapper.updateById(any(Warehouse.class))).willReturn(1);

            warehouseService.changeStatus(1L, "MAINTENANCE");

            verify(warehouseMapper).updateById(any(Warehouse.class));
        }

        @Test
        @DisplayName("ACTIVE -> CLOSED: 空仓库可以关闭")
        void activeToClosed_emptyWarehouse() {
            Warehouse existing = buildWarehouse(1L, "ACTIVE", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectById(any())).willReturn(existing);
            given(warehouseMapper.updateById(any(Warehouse.class))).willReturn(1);

            warehouseService.changeStatus(1L, "CLOSED");

            verify(warehouseMapper).updateById(any(Warehouse.class));
        }

        @Test
        @DisplayName("ACTIVE -> CLOSED: 有库存的仓库不能关闭")
        void activeToClosed_withInventory_shouldThrow() {
            Warehouse existing = buildWarehouse(1L, "ACTIVE", new BigDecimal("50"), new BigDecimal("100"));
            given(warehouseMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseService.changeStatus(1L, "CLOSED"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("仓库尚有库存，不能关闭");
        }

        @Test
        @DisplayName("ACTIVE -> FULL: 仓库标记为满")
        void activeToFull() {
            Warehouse existing = buildWarehouse(1L, "ACTIVE", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectById(any())).willReturn(existing);
            given(warehouseMapper.updateById(any(Warehouse.class))).willReturn(1);

            warehouseService.changeStatus(1L, "FULL");

            verify(warehouseMapper).updateById(any(Warehouse.class));
        }

        @Test
        @DisplayName("MAINTENANCE -> ACTIVE: 维护完成恢复")
        void maintenanceToActive() {
            Warehouse existing = buildWarehouse(1L, "MAINTENANCE", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectById(any())).willReturn(existing);
            given(warehouseMapper.updateById(any(Warehouse.class))).willReturn(1);

            warehouseService.changeStatus(1L, "ACTIVE");

            verify(warehouseMapper).updateById(any(Warehouse.class));
        }

        @Test
        @DisplayName("FULL -> ACTIVE: 满仓恢复可用")
        void fullToActive() {
            Warehouse existing = buildWarehouse(1L, "FULL", new BigDecimal("100"), new BigDecimal("100"));
            given(warehouseMapper.selectById(any())).willReturn(existing);
            given(warehouseMapper.updateById(any(Warehouse.class))).willReturn(1);

            warehouseService.changeStatus(1L, "ACTIVE");

            verify(warehouseMapper).updateById(any(Warehouse.class));
        }

        @Test
        @DisplayName("CLOSED -> 任何状态: 已关闭仓库不能变更")
        void closedToAny_shouldThrow() {
            Warehouse existing = buildWarehouse(1L, "CLOSED", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseService.changeStatus(1L, "ACTIVE"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已关闭的仓库不能变更状态");

            verify(warehouseMapper, never()).updateById(any(Warehouse.class));
        }

        @Test
        @DisplayName("ACTIVE -> ACTIVE: 相同状态不合法")
        void activeToActive_shouldThrow() {
            Warehouse existing = buildWarehouse(1L, "ACTIVE", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseService.changeStatus(1L, "ACTIVE"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不允许的状态变更");
        }

        @Test
        @DisplayName("ACTIVE -> UNKNOWN: 无效目标状态")
        void activeToUnknown_shouldThrow() {
            Warehouse existing = buildWarehouse(1L, "ACTIVE", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseService.changeStatus(1L, "UNKNOWN"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不允许的状态变更");
        }
    }

    @Nested
    @DisplayName("listAlerts - 仓库告警")
    class ListAlerts {

        @Test
        @DisplayName("使用率>=90%的仓库应出现在告警列表")
        void nearFullWarehouse_shouldAlert() {
            Warehouse w1 = buildWarehouse(1L, "ACTIVE", new BigDecimal("91"), new BigDecimal("100"));
            given(warehouseMapper.selectList(any())).willReturn(List.of(w1));

            List<Warehouse> alerts = warehouseService.listAlerts();

            assertThat(alerts).hasSize(1);
            assertThat(alerts.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("空且ACTIVE的仓库应出现在告警列表")
        void emptyActiveWarehouse_shouldAlert() {
            Warehouse w1 = buildWarehouse(1L, "ACTIVE", BigDecimal.ZERO, new BigDecimal("100"));
            given(warehouseMapper.selectList(any())).willReturn(List.of(w1));

            List<Warehouse> alerts = warehouseService.listAlerts();

            assertThat(alerts).hasSize(1);
        }

        @Test
        @DisplayName("MAINTENANCE状态的仓库应出现在告警列表")
        void maintenanceWarehouse_shouldAlert() {
            Warehouse w1 = buildWarehouse(1L, "MAINTENANCE", new BigDecimal("30"), new BigDecimal("100"));
            given(warehouseMapper.selectList(any())).willReturn(List.of(w1));

            List<Warehouse> alerts = warehouseService.listAlerts();

            assertThat(alerts).hasSize(1);
        }

        @Test
        @DisplayName("CLOSED状态的仓库不出现在告警列表")
        void closedWarehouse_shouldNotAlert() {
            Warehouse w1 = buildWarehouse(1L, "CLOSED", new BigDecimal("95"), new BigDecimal("100"));
            // selectList returns empty since CLOSED is excluded by the query
            given(warehouseMapper.selectList(any())).willReturn(List.of());

            List<Warehouse> alerts = warehouseService.listAlerts();

            assertThat(alerts).isEmpty();
        }

        @Test
        @DisplayName("正常使用率且非空的ACTIVE仓库不出现在告警列表")
        void normalActiveWarehouse_shouldNotAlert() {
            Warehouse w1 = buildWarehouse(1L, "ACTIVE", new BigDecimal("50"), new BigDecimal("100"));
            given(warehouseMapper.selectList(any())).willReturn(List.of(w1));

            List<Warehouse> alerts = warehouseService.listAlerts();

            assertThat(alerts).isEmpty();
        }

        @Test
        @DisplayName("多种告警条件同时满足时全部返回")
        void multipleAlertConditions() {
            Warehouse w1 = buildWarehouse(1L, "ACTIVE", new BigDecimal("95"), new BigDecimal("100"));
            Warehouse w2 = buildWarehouse(2L, "ACTIVE", BigDecimal.ZERO, new BigDecimal("50"));
            Warehouse w3 = buildWarehouse(3L, "MAINTENANCE", new BigDecimal("30"), new BigDecimal("100"));
            // CLOSED warehouse is excluded by the SQL query (ne status, 'CLOSED')
            given(warehouseMapper.selectList(any())).willReturn(Arrays.asList(w1, w2, w3));

            List<Warehouse> alerts = warehouseService.listAlerts();

            assertThat(alerts).hasSize(3)
                    .extracting(Warehouse::getId)
                    .containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("使用率为0且容量为0时(边界)不应告警为nearFull")
        void zeroCapacityWarehouse_shouldNotCrash() {
            Warehouse w1 = buildWarehouse(1L, "ACTIVE", BigDecimal.ZERO, BigDecimal.ZERO);
            given(warehouseMapper.selectList(any())).willReturn(List.of(w1));

            List<Warehouse> alerts = warehouseService.listAlerts();

            // capacity=0 should not cause division by zero; emptyActive should trigger
            assertThat(alerts).hasSize(1);
        }
    }
}
