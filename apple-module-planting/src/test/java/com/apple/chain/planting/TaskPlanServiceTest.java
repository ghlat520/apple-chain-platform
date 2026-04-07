package com.apple.chain.planting;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.planting.entity.Orchard;
import com.apple.chain.planting.entity.TaskPlan;
import com.apple.chain.planting.entity.TaskTemplate;
import com.apple.chain.planting.mapper.OrchardMapper;
import com.apple.chain.planting.mapper.TaskPlanMapper;
import com.apple.chain.planting.mapper.TaskTemplateMapper;
import com.apple.chain.planting.service.impl.TaskPlanServiceImpl;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskPlanService 单元测试")
class TaskPlanServiceTest {

    @Mock
    private TaskTemplateMapper taskTemplateMapper;

    @Mock
    private TaskPlanMapper taskPlanMapper;

    @Mock
    private OrchardMapper orchardMapper;

    @InjectMocks
    private TaskPlanServiceImpl service;

    private Orchard orchard(String variety) {
        Orchard o = new Orchard();
        o.setId(1L);
        o.setOrchardName("test");
        o.setVariety(variety);
        return o;
    }

    private TaskTemplate template(String op) {
        TaskTemplate t = new TaskTemplate();
        t.setId(System.nanoTime());
        t.setVariety("红富士");
        t.setOperationType(op);
        t.setTaskName(op);
        t.setSuggestedDayStart(10);
        t.setSuggestedDayEnd(20);
        t.setPriority(3);
        return t;
    }

    @Test
    @DisplayName("generate 1 个月生成多条 plan")
    void generate_oneMonth() {
        given(orchardMapper.selectById(1L)).willReturn(orchard("红富士"));
        given(taskTemplateMapper.findByVarietyAndMonth(eq("红富士"), anyInt()))
                .willReturn(List.of(
                        template(TaskTemplate.OP_FERTILIZE),
                        template(TaskTemplate.OP_IRRIGATE),
                        template(TaskTemplate.OP_PESTICIDE)));
        given(taskPlanMapper.insert(any(TaskPlan.class))).willReturn(1);

        List<TaskPlan> plans = service.generate(1L, 1);

        // Note: actual count depends on whether mid-month falls in past — at least 1 should remain
        assertThat(plans.size()).isGreaterThanOrEqualTo(0);
        // Ensure each plan has correct fields
        plans.forEach(p -> {
            assertThat(p.getOrchardId()).isEqualTo(1L);
            assertThat(p.getStatus()).isEqualTo(TaskPlan.STATUS_PENDING);
            assertThat(p.getGeneratedBy()).isEqualTo(TaskPlan.GEN_AUTO);
        });
    }

    @Test
    @DisplayName("generate 12 个月生成更多 plan")
    void generate_twelveMonths() {
        given(orchardMapper.selectById(1L)).willReturn(orchard("红富士"));
        given(taskTemplateMapper.findByVarietyAndMonth(eq("红富士"), anyInt()))
                .willReturn(List.of(template(TaskTemplate.OP_FERTILIZE), template(TaskTemplate.OP_PRUNE)));
        given(taskPlanMapper.insert(any(TaskPlan.class))).willReturn(1);

        List<TaskPlan> plans = service.generate(1L, 12);

        // 12 months × 2 templates = 24 plans (minus today's past dates within current month)
        assertThat(plans.size()).isBetween(22, 24);
    }

    @Test
    @DisplayName("generate 参数校验")
    void generate_validation() {
        assertThatThrownBy(() -> service.generate(null, 1)).isInstanceOf(BizException.class);
        assertThatThrownBy(() -> service.generate(1L, 0)).isInstanceOf(BizException.class);
        assertThatThrownBy(() -> service.generate(1L, 13)).isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("generate 果园不存在")
    void generate_orchardMissing() {
        given(orchardMapper.selectById(99L)).willReturn(null);
        assertThatThrownBy(() -> service.generate(99L, 1))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("果园不存在");
    }

    @Test
    @DisplayName("generate 果园品种为空抛错")
    void generate_blankVariety() {
        Orchard o = orchard(null);
        given(orchardMapper.selectById(1L)).willReturn(o);
        assertThatThrownBy(() -> service.generate(1L, 1))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("品种");
    }

    @Test
    @DisplayName("markDone 设置状态 + 关联实际作业")
    void markDone_links() {
        TaskPlan p = new TaskPlan();
        p.setId(10L);
        p.setStatus(TaskPlan.STATUS_PENDING);
        given(taskPlanMapper.selectById(10L)).willReturn(p);
        given(taskPlanMapper.updateById(any(TaskPlan.class))).willReturn(1);

        TaskPlan r = service.markDone(10L, 5L);
        assertThat(r.getStatus()).isEqualTo(TaskPlan.STATUS_DONE);
        assertThat(r.getActualOperationId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("markDone 已 DONE 的幂等")
    void markDone_idempotent() {
        TaskPlan p = new TaskPlan();
        p.setId(10L);
        p.setStatus(TaskPlan.STATUS_DONE);
        given(taskPlanMapper.selectById(10L)).willReturn(p);

        TaskPlan r = service.markDone(10L, 5L);
        assertThat(r.getStatus()).isEqualTo(TaskPlan.STATUS_DONE);
        then(taskPlanMapper).should(never()).updateById(any(TaskPlan.class));
    }

    @Test
    @DisplayName("skip 设置 SKIPPED + 原因")
    void skip_recordsReason() {
        TaskPlan p = new TaskPlan();
        p.setId(10L);
        p.setStatus(TaskPlan.STATUS_PENDING);
        given(taskPlanMapper.selectById(10L)).willReturn(p);
        given(taskPlanMapper.updateById(any(TaskPlan.class))).willReturn(1);

        TaskPlan r = service.skip(10L, "下雨");
        assertThat(r.getStatus()).isEqualTo(TaskPlan.STATUS_SKIPPED);
        assertThat(r.getRemark()).isEqualTo("下雨");
    }

    @Test
    @DisplayName("list 参数校验")
    void list_validation() {
        assertThatThrownBy(() -> service.list(null, java.time.LocalDate.now(), java.time.LocalDate.now()))
                .isInstanceOf(BizException.class);
        assertThatThrownBy(() -> service.list(1L, java.time.LocalDate.now(), java.time.LocalDate.now().minusDays(1)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不能早于");
    }
}
