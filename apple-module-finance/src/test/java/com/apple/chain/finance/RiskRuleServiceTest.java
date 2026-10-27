package com.apple.chain.finance;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.finance.dto.RiskRuleRequest;
import com.apple.chain.finance.entity.RiskRule;
import com.apple.chain.finance.enums.RiskSeverity;
import com.apple.chain.finance.mapper.RiskRuleMapper;
import com.apple.chain.finance.service.impl.RiskRuleServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RiskRuleServiceImpl}: CRUD + enable/disable + validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RiskRuleService - 风控规则服务")
class RiskRuleServiceTest {

    @Mock
    private RiskRuleMapper riskRuleMapper;

    private RiskRuleServiceImpl ruleService;

    @BeforeEach
    void setUp() {
        ruleService = new RiskRuleServiceImpl();
        ReflectionTestUtils.setField(ruleService, "baseMapper", riskRuleMapper);
    }

    private RiskRuleRequest validReq() {
        RiskRuleRequest req = new RiskRuleRequest();
        req.setName("贷款逾期超过30天");
        req.setRuleType("OVERDUE_DAYS");
        req.setThreshold(new BigDecimal("30"));
        req.setSeverity(RiskSeverity.HIGH);
        return req;
    }

    private RiskRule existing(Long id) {
        RiskRule rule = new RiskRule();
        rule.setId(id);
        rule.setName("旧名");
        rule.setRuleType("OVERDUE_DAYS");
        rule.setThreshold(new BigDecimal("15"));
        rule.setSeverity(RiskSeverity.MEDIUM);
        rule.setEnabled(Boolean.TRUE);
        rule.setCreatorId(1L);
        return rule;
    }

    @Nested
    @DisplayName("createRule - 创建规则")
    class CreateRule {

        @Test
        @DisplayName("默认 enabled = true，creatorId 落库")
        void should_default_enabled_true_and_persist_creator() {
            when(riskRuleMapper.insert(any(RiskRule.class))).thenReturn(1);

            RiskRuleRequest req = validReq();
            req.setEnabled(null); // not provided → default true

            ruleService.createRule(req, 42L);

            ArgumentCaptor<RiskRule> captor = ArgumentCaptor.forClass(RiskRule.class);
            verify(riskRuleMapper).insert(captor.capture());
            RiskRule saved = captor.getValue();
            assertThat(saved.getEnabled()).isTrue();
            assertThat(saved.getCreatorId()).isEqualTo(42L);
            assertThat(saved.getSeverity()).isEqualTo(RiskSeverity.HIGH);
            assertThat(saved.getThreshold()).isEqualByComparingTo("30");
        }

        @Test
        @DisplayName("空请求体抛 BizException")
        void null_request_throws() {
            assertThatThrownBy(() -> ruleService.createRule(null, 1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("请求体不能为空");
        }

        @Test
        @DisplayName("名称去除前后空白")
        void trims_name() {
            when(riskRuleMapper.insert(any(RiskRule.class))).thenReturn(1);
            RiskRuleRequest req = validReq();
            req.setName("  贷款逾期超过30天  ");
            req.setRuleType("  OVERDUE_DAYS  ");

            ruleService.createRule(req, 1L);

            ArgumentCaptor<RiskRule> captor = ArgumentCaptor.forClass(RiskRule.class);
            verify(riskRuleMapper).insert(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo("贷款逾期超过30天");
            assertThat(captor.getValue().getRuleType()).isEqualTo("OVERDUE_DAYS");
        }
    }

    @Nested
    @DisplayName("setEnabled - 启用/停用")
    class SetEnabled {

        @Test
        @DisplayName("从 enabled=true 切到 false 触发 update")
        void disable_enabled_rule() {
            RiskRule rule = existing(1L);
            rule.setEnabled(true);
            when(riskRuleMapper.selectById(1L)).thenReturn(rule, rule);
            when(riskRuleMapper.updateById(any(RiskRule.class))).thenReturn(1);

            ruleService.setEnabled(1L, false);

            ArgumentCaptor<RiskRule> captor = ArgumentCaptor.forClass(RiskRule.class);
            verify(riskRuleMapper).updateById(captor.capture());
            assertThat(captor.getValue().getEnabled()).isFalse();
        }

        @Test
        @DisplayName("状态没变时不调用 update（幂等）")
        void noop_when_state_unchanged() {
            RiskRule rule = existing(1L);
            rule.setEnabled(true);
            when(riskRuleMapper.selectById(1L)).thenReturn(rule);

            ruleService.setEnabled(1L, true);

            verify(riskRuleMapper, never()).updateById(any(RiskRule.class));
        }

        @Test
        @DisplayName("规则不存在抛 BizException")
        void throw_when_not_found() {
            when(riskRuleMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> ruleService.setEnabled(999L, true))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("风控规则不存在");
        }
    }

    @Nested
    @DisplayName("updateRule / deleteRule")
    class UpdateAndDelete {

        @Test
        @DisplayName("更新规则覆盖名称/阈值/严重度")
        void update_overrides_fields() {
            RiskRule rule = existing(1L);
            when(riskRuleMapper.selectById(1L)).thenReturn(rule, rule);
            when(riskRuleMapper.updateById(any(RiskRule.class))).thenReturn(1);

            RiskRuleRequest req = validReq();
            req.setName("新名称");
            req.setSeverity(RiskSeverity.CRITICAL);
            req.setThreshold(new BigDecimal("60"));

            ruleService.updateRule(1L, req);

            ArgumentCaptor<RiskRule> captor = ArgumentCaptor.forClass(RiskRule.class);
            verify(riskRuleMapper).updateById(captor.capture());
            RiskRule saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("新名称");
            assertThat(saved.getSeverity()).isEqualTo(RiskSeverity.CRITICAL);
            assertThat(saved.getThreshold()).isEqualByComparingTo("60");
        }

        @Test
        @DisplayName("删除不存在的规则抛异常，不调 deleteById")
        void delete_missing_throws() {
            when(riskRuleMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> ruleService.deleteRule(999L))
                    .isInstanceOf(BizException.class);
            verify(riskRuleMapper, never()).deleteById(anyLong());
        }
    }
}
