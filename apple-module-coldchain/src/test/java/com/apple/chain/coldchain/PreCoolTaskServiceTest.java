package com.apple.chain.coldchain;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.coldchain.entity.PreCoolTask;
import com.apple.chain.coldchain.mapper.PreCoolTaskMapper;
import com.apple.chain.coldchain.service.impl.PreCoolTaskServiceImpl;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("预冷任务服务 - 状态机测试")
class PreCoolTaskServiceTest {

    @Mock
    private PreCoolTaskMapper preCoolTaskMapper;

    private PreCoolTaskServiceImpl preCoolTaskService;

    @BeforeEach
    void setUp() throws Exception {
        preCoolTaskService = new PreCoolTaskServiceImpl();
        Field baseMapperField = preCoolTaskService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(preCoolTaskService, preCoolTaskMapper);
    }

    private PreCoolTask buildTask(Long id, String status) {
        PreCoolTask task = new PreCoolTask();
        task.setId(id);
        task.setStatus(status);
        return task;
    }

    @Nested
    @DisplayName("createTask - 创建任务")
    class CreateTask {

        @Test
        @DisplayName("创建任务时初始状态为PENDING，默认目标温度2.0")
        void shouldSetStatusPendingOnCreate() {
            PreCoolTask input = new PreCoolTask();
            input.setVehicleId(1L);

            given(preCoolTaskMapper.nextSeq(anyString())).willReturn(1);
            given(preCoolTaskMapper.insert(any(PreCoolTask.class))).willReturn(1);

            PreCoolTask result = preCoolTaskService.createTask(input);

            assertThat(result.getStatus()).isEqualTo("PENDING");
            assertThat(result.getTargetTemp()).isEqualByComparingTo(new BigDecimal("2.0"));
            assertThat(result.getTaskNo()).isNotNull();
        }

        @Test
        @DisplayName("创建任务时自定义目标温度不被覆盖")
        void shouldKeepCustomTargetTemp() {
            PreCoolTask input = new PreCoolTask();
            input.setTargetTemp(new BigDecimal("1.5"));

            given(preCoolTaskMapper.nextSeq(anyString())).willReturn(1);
            given(preCoolTaskMapper.insert(any(PreCoolTask.class))).willReturn(1);

            PreCoolTask result = preCoolTaskService.createTask(input);

            assertThat(result.getTargetTemp()).isEqualByComparingTo(new BigDecimal("1.5"));
        }
    }

    @Nested
    @DisplayName("start - 开始冷却")
    class Start {

        @Test
        @DisplayName("PENDING -> COOLING: 开始冷却成功")
        void pendingToCooling() {
            PreCoolTask task = buildTask(1L, "PENDING");
            given(preCoolTaskMapper.selectById(any())).willReturn(task);
            given(preCoolTaskMapper.updateById(any(PreCoolTask.class))).willReturn(1);

            PreCoolTask result = preCoolTaskService.start(1L);

            assertThat(result).isNotNull();
            verify(preCoolTaskMapper).updateById(argThat(t -> "COOLING".equals(t.getStatus())));
        }

        @Test
        @DisplayName("COOLING -> COOLING: 冷却中不能重复开始")
        void coolingToCooling_shouldThrow() {
            PreCoolTask task = buildTask(1L, "COOLING");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> preCoolTaskService.start(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待执行的任务可以开始冷却");
        }

        @Test
        @DisplayName("COMPLETED -> COOLING: 已完成不能重新开始")
        void completedToCooling_shouldThrow() {
            PreCoolTask task = buildTask(1L, "COMPLETED");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> preCoolTaskService.start(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待执行的任务可以开始冷却");
        }

        @Test
        @DisplayName("FAILED -> COOLING: 失败任务不能重新开始")
        void failedToCooling_shouldThrow() {
            PreCoolTask task = buildTask(1L, "FAILED");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> preCoolTaskService.start(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待执行的任务可以开始冷却");
        }
    }

    @Nested
    @DisplayName("complete - 完成冷却")
    class Complete {

        @Test
        @DisplayName("COOLING -> COMPLETED: 完成冷却成功并计算耗时")
        void coolingToCompleted() {
            PreCoolTask task = buildTask(1L, "COOLING");
            task.setStartTime(java.time.LocalDateTime.now().minusMinutes(30));
            given(preCoolTaskMapper.selectById(any())).willReturn(task);
            given(preCoolTaskMapper.updateById(any(PreCoolTask.class))).willReturn(1);

            PreCoolTask result = preCoolTaskService.complete(1L);

            assertThat(result).isNotNull();
            verify(preCoolTaskMapper).updateById(argThat(t ->
                    "COMPLETED".equals(t.getStatus()) && t.getEndTime() != null && t.getDuration() != null));
        }

        @Test
        @DisplayName("PENDING -> COMPLETED: 待执行不能直接完成")
        void pendingToCompleted_shouldThrow() {
            PreCoolTask task = buildTask(1L, "PENDING");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> preCoolTaskService.complete(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有冷却中的任务可以完成");
        }

        @Test
        @DisplayName("COMPLETED -> COMPLETED: 重复完成不允许")
        void completedToCompleted_shouldThrow() {
            PreCoolTask task = buildTask(1L, "COMPLETED");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> preCoolTaskService.complete(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有冷却中的任务可以完成");
        }

        @Test
        @DisplayName("startTime为null时duration为0")
        void coolingToCompleted_nullStartTime() {
            PreCoolTask task = buildTask(1L, "COOLING");
            task.setStartTime(null);
            given(preCoolTaskMapper.selectById(any())).willReturn(task);
            given(preCoolTaskMapper.updateById(any(PreCoolTask.class))).willReturn(1);

            PreCoolTask result = preCoolTaskService.complete(1L);

            assertThat(result).isNotNull();
            verify(preCoolTaskMapper).updateById(argThat(t -> t.getDuration() == 0));
        }
    }

    @Nested
    @DisplayName("deleteTask - 删除任务")
    class DeleteTask {

        @Test
        @DisplayName("PENDING任务可以删除")
        void pendingTaskCanBeDeleted() {
            PreCoolTask task = buildTask(1L, "PENDING");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);
            given(preCoolTaskMapper.deleteById(1L)).willReturn(1);

            preCoolTaskService.deleteTask(1L);

            verify(preCoolTaskMapper).deleteById(1L);
        }

        @Test
        @DisplayName("COMPLETED任务可以删除")
        void completedTaskCanBeDeleted() {
            PreCoolTask task = buildTask(1L, "COMPLETED");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);
            given(preCoolTaskMapper.deleteById(1L)).willReturn(1);

            preCoolTaskService.deleteTask(1L);

            verify(preCoolTaskMapper).deleteById(1L);
        }

        @Test
        @DisplayName("COOLING任务不能删除")
        void coolingTaskCannotBeDeleted() {
            PreCoolTask task = buildTask(1L, "COOLING");
            given(preCoolTaskMapper.selectById(1L)).willReturn(task);

            assertThatThrownBy(() -> preCoolTaskService.deleteTask(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("冷却中的任务不能删除");
        }
    }

    @Nested
    @DisplayName("updateTask - 更新任务")
    class UpdateTask {

        @Test
        @DisplayName("PENDING任务可以修改")
        void pendingTaskCanBeUpdated() {
            PreCoolTask existing = buildTask(1L, "PENDING");
            given(preCoolTaskMapper.selectById(any())).willReturn(existing);
            given(preCoolTaskMapper.updateById(any(PreCoolTask.class))).willReturn(1);

            PreCoolTask update = new PreCoolTask();
            update.setOperator("张三");

            PreCoolTask result = preCoolTaskService.updateTask(1L, update);

            assertThat(result).isNotNull();
            verify(preCoolTaskMapper).updateById(any(PreCoolTask.class));
        }

        @Test
        @DisplayName("COOLING任务不允许修改")
        void coolingTaskCannotBeUpdated() {
            PreCoolTask existing = buildTask(1L, "COOLING");
            given(preCoolTaskMapper.selectById(1L)).willReturn(existing);

            PreCoolTask update = new PreCoolTask();

            assertThatThrownBy(() -> preCoolTaskService.updateTask(1L, update))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("冷却中或已完成的任务不允许修改");
        }

        @Test
        @DisplayName("COMPLETED任务不允许修改")
        void completedTaskCannotBeUpdated() {
            PreCoolTask existing = buildTask(1L, "COMPLETED");
            given(preCoolTaskMapper.selectById(1L)).willReturn(existing);

            PreCoolTask update = new PreCoolTask();

            assertThatThrownBy(() -> preCoolTaskService.updateTask(1L, update))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("冷却中或已完成的任务不允许修改");
        }
    }

    private static PreCoolTask argThat(java.util.function.Predicate<PreCoolTask> predicate) {
        return org.mockito.ArgumentMatchers.argThat(predicate::test);
    }
}
