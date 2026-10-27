package com.apple.chain.finance;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.finance.dto.RiskEventHandleRequest;
import com.apple.chain.finance.entity.RiskEvent;
import com.apple.chain.finance.enums.RiskEventStatus;
import com.apple.chain.finance.enums.RiskSeverity;
import com.apple.chain.finance.mapper.RiskEventMapper;
import com.apple.chain.finance.service.impl.RiskEventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RiskEventServiceImpl}: lifecycle transitions + queries.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RiskEventService - 风控事件服务")
class RiskEventServiceTest {

    @Mock
    private RiskEventMapper riskEventMapper;

    private RiskEventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new RiskEventServiceImpl();
        ReflectionTestUtils.setField(eventService, "baseMapper", riskEventMapper);
    }

    private RiskEvent makeEvent(Long id, RiskEventStatus status) {
        RiskEvent event = new RiskEvent();
        event.setId(id);
        event.setRuleId(2801L);
        event.setTargetType("LOAN");
        event.setTargetId(10001L);
        event.setSeverity(RiskSeverity.HIGH);
        event.setTriggerValue(new BigDecimal("35"));
        event.setStatus(status);
        event.setTriggerTime(LocalDateTime.now().minusHours(2));
        return event;
    }

    @Nested
    @DisplayName("handleEvent - 状态机")
    class HandleEvent {

        @Test
        @DisplayName("PENDING → HANDLING 不写 handleTime，可设 assigneeId/备注")
        void pending_to_handling() {
            RiskEvent event = makeEvent(1L, RiskEventStatus.PENDING);
            when(riskEventMapper.selectById(1L)).thenReturn(event, event);
            when(riskEventMapper.updateById(any(RiskEvent.class))).thenReturn(1);

            RiskEventHandleRequest req = new RiskEventHandleRequest();
            req.setStatus(RiskEventStatus.HANDLING);
            req.setAssigneeId(7L);
            req.setHandleRemark("我来跟进");

            eventService.handleEvent(1L, req);

            ArgumentCaptor<RiskEvent> captor = ArgumentCaptor.forClass(RiskEvent.class);
            verify(riskEventMapper).updateById(captor.capture());
            RiskEvent saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(RiskEventStatus.HANDLING);
            assertThat(saved.getAssigneeId()).isEqualTo(7L);
            assertThat(saved.getHandleRemark()).isEqualTo("我来跟进");
            assertThat(saved.getHandleTime()).isNull();
        }

        @Test
        @DisplayName("HANDLING → RESOLVED 自动写入 handleTime")
        void handling_to_resolved_sets_handle_time() {
            RiskEvent event = makeEvent(1L, RiskEventStatus.HANDLING);
            when(riskEventMapper.selectById(1L)).thenReturn(event, event);
            when(riskEventMapper.updateById(any(RiskEvent.class))).thenReturn(1);

            RiskEventHandleRequest req = new RiskEventHandleRequest();
            req.setStatus(RiskEventStatus.RESOLVED);
            req.setHandleRemark("已和借款人达成展期协议");

            eventService.handleEvent(1L, req);

            ArgumentCaptor<RiskEvent> captor = ArgumentCaptor.forClass(RiskEvent.class);
            verify(riskEventMapper).updateById(captor.capture());
            RiskEvent saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(RiskEventStatus.RESOLVED);
            assertThat(saved.getHandleTime()).isNotNull();
        }

        @Test
        @DisplayName("PENDING → IGNORED 也算结案，handleTime 必填")
        void pending_to_ignored_is_terminal() {
            RiskEvent event = makeEvent(1L, RiskEventStatus.PENDING);
            when(riskEventMapper.selectById(1L)).thenReturn(event, event);
            when(riskEventMapper.updateById(any(RiskEvent.class))).thenReturn(1);

            RiskEventHandleRequest req = new RiskEventHandleRequest();
            req.setStatus(RiskEventStatus.IGNORED);
            req.setHandleRemark("误报");

            eventService.handleEvent(1L, req);

            ArgumentCaptor<RiskEvent> captor = ArgumentCaptor.forClass(RiskEvent.class);
            verify(riskEventMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(RiskEventStatus.IGNORED);
            assertThat(captor.getValue().getHandleTime()).isNotNull();
        }

        @Test
        @DisplayName("RESOLVED 已结案不能再处置")
        void terminal_cannot_be_reopened() {
            RiskEvent event = makeEvent(1L, RiskEventStatus.RESOLVED);
            when(riskEventMapper.selectById(1L)).thenReturn(event);

            RiskEventHandleRequest req = new RiskEventHandleRequest();
            req.setStatus(RiskEventStatus.HANDLING);

            assertThatThrownBy(() -> eventService.handleEvent(1L, req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("已结案");
            verify(riskEventMapper, never()).updateById(any(RiskEvent.class));
        }

        @Test
        @DisplayName("禁止把状态回退到 PENDING")
        void cannot_revert_to_pending() {
            RiskEvent event = makeEvent(1L, RiskEventStatus.HANDLING);
            when(riskEventMapper.selectById(1L)).thenReturn(event);

            RiskEventHandleRequest req = new RiskEventHandleRequest();
            req.setStatus(RiskEventStatus.PENDING);

            assertThatThrownBy(() -> eventService.handleEvent(1L, req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("回退");
        }

        @Test
        @DisplayName("空 status 抛 BizException")
        void null_status_throws() {
            RiskEventHandleRequest req = new RiskEventHandleRequest();
            assertThatThrownBy(() -> eventService.handleEvent(1L, req))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("目标状态");
            verify(riskEventMapper, never()).selectById(anyLong());
        }
    }

    @Nested
    @DisplayName("getEventDetail")
    class GetEventDetail {

        @Test
        @DisplayName("不存在时抛 BizException")
        void throw_when_not_found() {
            when(riskEventMapper.selectById(999L)).thenReturn(null);
            assertThatThrownBy(() -> eventService.getEventDetail(999L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("风控事件不存在");
        }
    }
}
