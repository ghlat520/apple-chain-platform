package com.apple.chain.warehouse;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.warehouse.entity.Warehouse;
import com.apple.chain.warehouse.entity.WarehouseReceipt;
import com.apple.chain.warehouse.mapper.WarehouseReceiptMapper;
import com.apple.chain.warehouse.service.WarehouseService;
import com.apple.chain.warehouse.service.impl.WarehouseReceiptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("仓单服务 - 状态机测试")
class WarehouseReceiptServiceTest {

    @Mock
    private WarehouseReceiptMapper warehouseReceiptMapper;

    @Mock
    private WarehouseService warehouseService;

    private WarehouseReceiptServiceImpl warehouseReceiptService;

    @BeforeEach
    void setUp() throws Exception {
        warehouseReceiptService = new WarehouseReceiptServiceImpl(warehouseService);
        // Inject baseMapper via reflection (required by ServiceImpl)
        Field baseMapperField = warehouseReceiptService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(warehouseReceiptService, warehouseReceiptMapper);
    }

    private WarehouseReceipt buildReceipt(Long id, String status) {
        WarehouseReceipt receipt = new WarehouseReceipt();
        receipt.setId(id);
        receipt.setStatus(status);
        return receipt;
    }

    @Nested
    @DisplayName("createReceipt - 创建仓单")
    class CreateReceipt {

        @Test
        @DisplayName("创建仓单时自动设置状态为VALID")
        void shouldSetStatusValidOnCreate() {
            WarehouseReceipt input = new WarehouseReceipt();
            input.setWarehouseId(1L);
            input.setQuantity(new BigDecimal("100"));
            input.setUnitValue(new BigDecimal("5.00"));

            Warehouse warehouse = new Warehouse();
            warehouse.setName("冷库A");

            given(warehouseService.getWarehouseDetail(1L)).willReturn(warehouse);
            given(warehouseReceiptMapper.nextSeq(anyString())).willReturn(1);
            given(warehouseReceiptMapper.insert(any(WarehouseReceipt.class))).willReturn(1);

            WarehouseReceipt result = warehouseReceiptService.createReceipt(input);

            assertThat(result.getStatus()).isEqualTo("VALID");
            assertThat(result.getWarehouseName()).isEqualTo("冷库A");
            assertThat(result.getTotalValue())
                    .isEqualByComparingTo(new BigDecimal("500.00"));
        }
    }

    @Nested
    @DisplayName("changeStatus - 状态机流转")
    class ChangeStatus {

        @Test
        @DisplayName("VALID -> PLEDGED: 有效仓单质押")
        void validToPledged() {
            WarehouseReceipt existing = buildReceipt(1L, "VALID");
            given(warehouseReceiptMapper.selectById(any())).willReturn(existing);
            given(warehouseReceiptMapper.updateById(any(WarehouseReceipt.class))).willReturn(1);

            warehouseReceiptService.changeStatus(1L, "PLEDGED");

            verify(warehouseReceiptMapper).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("VALID -> TRANSFERRED: 有效仓单转让")
        void validToTransferred() {
            WarehouseReceipt existing = buildReceipt(1L, "VALID");
            given(warehouseReceiptMapper.selectById(any())).willReturn(existing);
            given(warehouseReceiptMapper.updateById(any(WarehouseReceipt.class))).willReturn(1);

            warehouseReceiptService.changeStatus(1L, "TRANSFERRED");

            verify(warehouseReceiptMapper).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("VALID -> CANCELLED: 有效仓单注销")
        void validToCancelled() {
            WarehouseReceipt existing = buildReceipt(1L, "VALID");
            given(warehouseReceiptMapper.selectById(any())).willReturn(existing);
            given(warehouseReceiptMapper.updateById(any(WarehouseReceipt.class))).willReturn(1);

            warehouseReceiptService.changeStatus(1L, "CANCELLED");

            verify(warehouseReceiptMapper).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("PLEDGED -> CANCELLED: 质押仓单注销")
        void pledgedToCancelled() {
            WarehouseReceipt existing = buildReceipt(1L, "PLEDGED");
            given(warehouseReceiptMapper.selectById(any())).willReturn(existing);
            given(warehouseReceiptMapper.updateById(any(WarehouseReceipt.class))).willReturn(1);

            warehouseReceiptService.changeStatus(1L, "CANCELLED");

            verify(warehouseReceiptMapper).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("PLEDGED -> TRANSFERRED: 质押仓单不允许转让")
        void pledgedToTransferred_shouldThrow() {
            WarehouseReceipt existing = buildReceipt(1L, "PLEDGED");
            given(warehouseReceiptMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseReceiptService.changeStatus(1L, "TRANSFERRED"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不允许的状态变更");

            verify(warehouseReceiptMapper, never()).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("TRANSFERRED -> 任何状态: 已转让仓单不允许变更")
        void transferredToAny_shouldThrow() {
            WarehouseReceipt existing = buildReceipt(1L, "TRANSFERRED");
            given(warehouseReceiptMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseReceiptService.changeStatus(1L, "CANCELLED"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不允许的状态变更");

            verify(warehouseReceiptMapper, never()).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("CANCELLED -> 任何状态: 已注销仓单不允许变更")
        void cancelledToAny_shouldThrow() {
            WarehouseReceipt existing = buildReceipt(1L, "CANCELLED");
            given(warehouseReceiptMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseReceiptService.changeStatus(1L, "VALID"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不允许的状态变更");

            verify(warehouseReceiptMapper, never()).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("无效状态值: 抛出异常")
        void invalidStatusValue_shouldThrow() {
            assertThatThrownBy(() -> warehouseReceiptService.changeStatus(1L, "UNKNOWN_STATUS"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("无效的仓单状态");
        }

        @Test
        @DisplayName("VALID -> VALID: 相同状态不合法")
        void validToSame_shouldThrow() {
            WarehouseReceipt existing = buildReceipt(1L, "VALID");
            given(warehouseReceiptMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseReceiptService.changeStatus(1L, "VALID"))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("不允许的状态变更");
        }
    }

    @Nested
    @DisplayName("deleteReceipt - 删除仓单")
    class DeleteReceipt {

        @Test
        @DisplayName("VALID仓单可以删除")
        void validReceiptCanBeDeleted() {
            WarehouseReceipt existing = buildReceipt(1L, "VALID");
            given(warehouseReceiptMapper.selectById(1L)).willReturn(existing);
            given(warehouseReceiptMapper.deleteById(1L)).willReturn(1);

            warehouseReceiptService.deleteReceipt(1L);

            verify(warehouseReceiptMapper).deleteById(1L);
        }

        @Test
        @DisplayName("PLEDGED仓单不能删除")
        void pledgedReceiptCannotBeDeleted() {
            WarehouseReceipt existing = buildReceipt(1L, "PLEDGED");
            given(warehouseReceiptMapper.selectById(1L)).willReturn(existing);

            assertThatThrownBy(() -> warehouseReceiptService.deleteReceipt(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("质押中的仓单不能删除");
        }
    }

    @Nested
    @DisplayName("updateReceipt - 更新仓单")
    class UpdateReceipt {

        @Test
        @DisplayName("VALID仓单可以修改")
        void validReceiptCanBeUpdated() {
            WarehouseReceipt existing = buildReceipt(1L, "VALID");
            given(warehouseReceiptMapper.selectById(any())).willReturn(existing);
            given(warehouseReceiptMapper.updateById(any(WarehouseReceipt.class))).willReturn(1);

            WarehouseReceipt update = new WarehouseReceipt();
            update.setQuantity(new BigDecimal("200"));
            update.setUnitValue(new BigDecimal("6.00"));

            WarehouseReceipt result = warehouseReceiptService.updateReceipt(1L, update);

            assertThat(result).isNotNull();
            verify(warehouseReceiptMapper).updateById(any(WarehouseReceipt.class));
        }

        @Test
        @DisplayName("PLEDGED仓单不允许修改")
        void pledgedReceiptCannotBeUpdated() {
            WarehouseReceipt existing = buildReceipt(1L, "PLEDGED");
            given(warehouseReceiptMapper.selectById(1L)).willReturn(existing);

            WarehouseReceipt update = new WarehouseReceipt();

            assertThatThrownBy(() -> warehouseReceiptService.updateReceipt(1L, update))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有有效状态的仓单可以修改");
        }
    }
}
