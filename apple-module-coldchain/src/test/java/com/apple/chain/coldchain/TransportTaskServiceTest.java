package com.apple.chain.coldchain;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.coldchain.entity.TransportTask;
import com.apple.chain.coldchain.entity.Vehicle;
import com.apple.chain.coldchain.mapper.TransportTaskMapper;
import com.apple.chain.coldchain.mapper.VehicleMapper;
import com.apple.chain.coldchain.service.impl.TransportTaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("运输任务服务 - 生命周期测试")
class TransportTaskServiceTest {

    @Mock
    private TransportTaskMapper transportTaskMapper;

    @Mock
    private VehicleMapper vehicleMapper;

    private TransportTaskServiceImpl transportTaskService;

    @BeforeEach
    void setUp() throws Exception {
        transportTaskService = new TransportTaskServiceImpl(vehicleMapper);
        Field baseMapperField = transportTaskService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(transportTaskService, transportTaskMapper);
    }

    private TransportTask buildTask(Long id, String status, Long vehicleId) {
        TransportTask task = new TransportTask();
        task.setId(id);
        task.setStatus(status);
        task.setVehicleId(vehicleId);
        return task;
    }

    @Nested
    @DisplayName("createTask - 创建任务")
    class CreateTask {

        @Test
        @DisplayName("创建任务时初始状态为PENDING")
        void shouldSetStatusPendingOnCreate() {
            TransportTask input = new TransportTask();
            input.setVehicleId(1L);

            given(transportTaskMapper.nextSeq(anyString())).willReturn(1);
            given(transportTaskMapper.insert(any(TransportTask.class))).willReturn(1);

            TransportTask result = transportTaskService.createTask(input);

            assertThat(result.getStatus()).isEqualTo("PENDING");
            assertThat(result.getTaskCode()).isNotNull();
        }
    }

    @Nested
    @DisplayName("depart - 发车")
    class Depart {

        @Test
        @DisplayName("PENDING -> IN_TRANSIT: 发车成功并同步车辆状态")
        void pendingToInTransit_withVehicle() {
            TransportTask task = buildTask(1L, "PENDING", 10L);
            given(transportTaskMapper.selectById(any())).willReturn(task);
            given(transportTaskMapper.updateById(any(TransportTask.class))).willReturn(1);
            given(vehicleMapper.updateById(any(Vehicle.class))).willReturn(1);

            TransportTask result = transportTaskService.depart(1L);

            assertThat(result).isNotNull();
            verify(vehicleMapper).updateById(argThatVehicle(v ->
                    v.getId().equals(10L) && "IN_TRANSIT".equals(v.getStatus())));
        }

        @Test
        @DisplayName("PENDING -> IN_TRANSIT: 无车辆时不报错")
        void pendingToInTransit_withoutVehicle() {
            TransportTask task = buildTask(1L, "PENDING", null);
            given(transportTaskMapper.selectById(any())).willReturn(task);
            given(transportTaskMapper.updateById(any(TransportTask.class))).willReturn(1);

            TransportTask result = transportTaskService.depart(1L);

            assertThat(result).isNotNull();
            verify(vehicleMapper, never()).updateById(any(Vehicle.class));
        }

        @Test
        @DisplayName("IN_TRANSIT -> 发车: 已在运输中不能重复发车")
        void inTransitToDepart_shouldThrow() {
            TransportTask task = buildTask(1L, "IN_TRANSIT", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.depart(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待发车的任务可以发车");

            verify(vehicleMapper, never()).updateById(any(Vehicle.class));
        }

        @Test
        @DisplayName("DELIVERED -> 发车: 已送达不能发车")
        void deliveredToDepart_shouldThrow() {
            TransportTask task = buildTask(1L, "DELIVERED", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.depart(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待发车的任务可以发车");
        }

        @Test
        @DisplayName("CANCELLED -> 发车: 已取消不能发车")
        void cancelledToDepart_shouldThrow() {
            TransportTask task = buildTask(1L, "CANCELLED", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.depart(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待发车的任务可以发车");
        }
    }

    @Nested
    @DisplayName("deliver - 送达")
    class Deliver {

        @Test
        @DisplayName("IN_TRANSIT -> DELIVERED: 确认送达并同步车辆状态为IDLE")
        void inTransitToDelivered_withVehicle() {
            TransportTask task = buildTask(1L, "IN_TRANSIT", 10L);
            given(transportTaskMapper.selectById(any())).willReturn(task);
            given(transportTaskMapper.updateById(any(TransportTask.class))).willReturn(1);
            given(vehicleMapper.updateById(any(Vehicle.class))).willReturn(1);

            TransportTask result = transportTaskService.deliver(1L);

            assertThat(result).isNotNull();
            verify(vehicleMapper).updateById(argThatVehicle(v ->
                    v.getId().equals(10L) && "IDLE".equals(v.getStatus())));
        }

        @Test
        @DisplayName("IN_TRANSIT -> DELIVERED: 无车辆时不报错")
        void inTransitToDelivered_withoutVehicle() {
            TransportTask task = buildTask(1L, "IN_TRANSIT", null);
            given(transportTaskMapper.selectById(any())).willReturn(task);
            given(transportTaskMapper.updateById(any(TransportTask.class))).willReturn(1);

            TransportTask result = transportTaskService.deliver(1L);

            assertThat(result).isNotNull();
            verify(vehicleMapper, never()).updateById(any(Vehicle.class));
        }

        @Test
        @DisplayName("PENDING -> 送达: 待发车不能确认送达")
        void pendingToDeliver_shouldThrow() {
            TransportTask task = buildTask(1L, "PENDING", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.deliver(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有运输中的任务可以确认送达");
        }

        @Test
        @DisplayName("DELIVERED -> 送达: 重复送达不允许")
        void deliveredToDeliver_shouldThrow() {
            TransportTask task = buildTask(1L, "DELIVERED", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.deliver(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有运输中的任务可以确认送达");
        }

        @Test
        @DisplayName("CANCELLED -> 送达: 已取消不能送达")
        void cancelledToDeliver_shouldThrow() {
            TransportTask task = buildTask(1L, "CANCELLED", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.deliver(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有运输中的任务可以确认送达");
        }
    }

    @Nested
    @DisplayName("cancelTask - 取消任务")
    class CancelTask {

        @Test
        @DisplayName("PENDING -> CANCELLED: 待发车任务可以取消")
        void pendingToCancelled() {
            TransportTask task = buildTask(1L, "PENDING", 10L);
            given(transportTaskMapper.selectById(any())).willReturn(task);
            given(transportTaskMapper.updateById(any(TransportTask.class))).willReturn(1);

            TransportTask result = transportTaskService.cancelTask(1L);

            assertThat(result).isNotNull();
            verify(transportTaskMapper).updateById(argThatTask(t ->
                    "CANCELLED".equals(t.getStatus())));
        }

        @Test
        @DisplayName("IN_TRANSIT -> CANCELLED: 运输中不能取消")
        void inTransitToCancelled_shouldThrow() {
            TransportTask task = buildTask(1L, "IN_TRANSIT", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.cancelTask(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待发车的任务可以取消");
        }

        @Test
        @DisplayName("DELIVERED -> CANCELLED: 已送达不能取消")
        void deliveredToCancelled_shouldThrow() {
            TransportTask task = buildTask(1L, "DELIVERED", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.cancelTask(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待发车的任务可以取消");
        }
    }

    @Nested
    @DisplayName("deleteTask - 删除任务")
    class DeleteTask {

        @Test
        @DisplayName("PENDING任务可以删除")
        void pendingTaskCanBeDeleted() {
            TransportTask task = buildTask(1L, "PENDING", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);
            given(transportTaskMapper.deleteById(1L)).willReturn(1);

            transportTaskService.deleteTask(1L);

            verify(transportTaskMapper).deleteById(1L);
        }

        @Test
        @DisplayName("IN_TRANSIT任务不能删除")
        void inTransitTaskCannotBeDeleted() {
            TransportTask task = buildTask(1L, "IN_TRANSIT", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> transportTaskService.deleteTask(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("运输中的任务不能删除");
        }
    }

    @Nested
    @DisplayName("updateTask - 更新任务")
    class UpdateTask {

        @Test
        @DisplayName("DELIVERED任务不允许修改")
        void deliveredTaskCannotBeUpdated() {
            TransportTask existing = buildTask(1L, "DELIVERED", 10L);
            given(transportTaskMapper.selectById(1L)).willReturn(existing);

            TransportTask update = new TransportTask();

            assertThatThrownBy(() -> transportTaskService.updateTask(1L, update))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已送达的任务不允许修改");
        }

        @Test
        @DisplayName("PENDING任务可以修改")
        void pendingTaskCanBeUpdated() {
            TransportTask existing = buildTask(1L, "PENDING", 10L);
            given(transportTaskMapper.selectById(any())).willReturn(existing);
            given(transportTaskMapper.updateById(any(TransportTask.class))).willReturn(1);

            TransportTask update = new TransportTask();
            update.setCargoDesc("测试货物");

            TransportTask result = transportTaskService.updateTask(1L, update);

            assertThat(result).isNotNull();
            verify(transportTaskMapper).updateById(any(TransportTask.class));
        }
    }

    private static Vehicle argThatVehicle(java.util.function.Predicate<Vehicle> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }

    private static TransportTask argThatTask(java.util.function.Predicate<TransportTask> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
