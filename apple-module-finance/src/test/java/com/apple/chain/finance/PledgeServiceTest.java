package com.apple.chain.finance;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.finance.entity.Pledge;
import com.apple.chain.finance.mapper.PledgeMapper;
import com.apple.chain.finance.service.impl.PledgeServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * M7 PledgeService unit tests.
 *
 * Tests pledge lifecycle:
 *   PENDING -> ACTIVE -> RELEASED
 *                   -> DEFAULTED
 *
 * And auto-calculation of loanAmount from appraisedValue * pledgeRate.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PledgeService - 质押服务")
class PledgeServiceTest {

    @Mock
    private PledgeMapper pledgeMapper;

    private PledgeServiceImpl pledgeService;

    @BeforeEach
    void setUp() {
        pledgeService = new PledgeServiceImpl();
        ReflectionTestUtils.setField(pledgeService, "baseMapper", pledgeMapper);
    }

    // ── Helper ──────────────────────────────────────────────────────

    private Pledge makePledge(Long id, String status) {
        Pledge pledge = new Pledge();
        pledge.setId(id);
        pledge.setPledgeCode("PL202604010001");
        pledge.setPledgorName("Test Farm");
        pledge.setPledgeeName("Bank");
        pledge.setCommodity("Apple");
        pledge.setQuantity(new BigDecimal("100"));
        pledge.setUnit("ton");
        pledge.setStatus(status);
        return pledge;
    }

    // ── createPledge ────────────────────────────────────────────────

    @Nested
    @DisplayName("createPledge - 创建质押")
    class CreatePledge {

        @Test
        @DisplayName("自动根据评估值和质押率计算可贷金额")
        void should_auto_calc_loan_amount() {
            when(pledgeMapper.nextSeq(anyString())).thenReturn(1);
            when(pledgeMapper.insert(any(Pledge.class))).thenReturn(1);

            Pledge input = makePledge(null, null);
            input.setAppraisedValue(new BigDecimal("1000000")); // 100万
            input.setPledgeRate(new BigDecimal("0.7"));         // 70%

            Pledge result = pledgeService.createPledge(input);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).insert(captor.capture());
            Pledge saved = captor.getValue();
            assertThat(saved.getLoanAmount())
                    .isEqualByComparingTo("700000"); // 100万 * 70% = 70万
        }

        @Test
        @DisplayName("评估值为空时不计算可贷金额")
        void no_calc_when_appraised_value_null() {
            when(pledgeMapper.nextSeq(anyString())).thenReturn(1);
            when(pledgeMapper.insert(any(Pledge.class))).thenReturn(1);

            Pledge input = makePledge(null, null);
            input.setAppraisedValue(null);
            input.setPledgeRate(new BigDecimal("0.7"));

            pledgeService.createPledge(input);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).insert(captor.capture());
            assertThat(captor.getValue().getLoanAmount()).isNull();
        }

        @Test
        @DisplayName("质押率为空时不计算可贷金额")
        void no_calc_when_pledge_rate_null() {
            when(pledgeMapper.nextSeq(anyString())).thenReturn(1);
            when(pledgeMapper.insert(any(Pledge.class))).thenReturn(1);

            Pledge input = makePledge(null, null);
            input.setAppraisedValue(new BigDecimal("1000000"));
            input.setPledgeRate(null);

            pledgeService.createPledge(input);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).insert(captor.capture());
            assertThat(captor.getValue().getLoanAmount()).isNull();
        }

        @Test
        @DisplayName("创建时状态为 PENDING")
        void initial_status_is_pending() {
            when(pledgeMapper.nextSeq(anyString())).thenReturn(1);
            when(pledgeMapper.insert(any(Pledge.class))).thenReturn(1);

            Pledge input = makePledge(null, null);

            pledgeService.createPledge(input);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).insert(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("质押编号按日期+序号生成")
        void pledge_code_format() {
            when(pledgeMapper.nextSeq(anyString())).thenReturn(7);
            when(pledgeMapper.insert(any(Pledge.class))).thenReturn(1);

            pledgeService.createPledge(makePledge(null, null));

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).insert(captor.capture());
            String code = captor.getValue().getPledgeCode();
            assertThat(code).startsWith("PL");
            assertThat(code).endsWith("0007");
        }

        @Test
        @DisplayName("边界: 评估值为零时贷款金额为零")
        void zero_appraised_value() {
            when(pledgeMapper.nextSeq(anyString())).thenReturn(1);
            when(pledgeMapper.insert(any(Pledge.class))).thenReturn(1);

            Pledge input = makePledge(null, null);
            input.setAppraisedValue(BigDecimal.ZERO);
            input.setPledgeRate(new BigDecimal("0.8"));

            pledgeService.createPledge(input);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).insert(captor.capture());
            assertThat(captor.getValue().getLoanAmount())
                    .isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("边界: 100% 质押率时贷款金额等于评估值")
        void full_pledge_rate() {
            when(pledgeMapper.nextSeq(anyString())).thenReturn(1);
            when(pledgeMapper.insert(any(Pledge.class))).thenReturn(1);

            Pledge input = makePledge(null, null);
            input.setAppraisedValue(new BigDecimal("500000"));
            input.setPledgeRate(new BigDecimal("1.0"));

            pledgeService.createPledge(input);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).insert(captor.capture());
            assertThat(captor.getValue().getLoanAmount())
                    .isEqualByComparingTo("500000");
        }
    }

    // ── activate ────────────────────────────────────────────────────

    @Nested
    @DisplayName("activate - 激活质押")
    class Activate {

        @Test
        @DisplayName("PENDING 质押可以激活")
        void activate_pending() {
            Pledge pledge = makePledge(1L, "PENDING");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge, pledge);
            when(pledgeMapper.updateById(any(Pledge.class))).thenReturn(1);

            pledgeService.activate(1L);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
            assertThat(captor.getValue().getStartDate()).isNotNull();
        }

        @Test
        @DisplayName("非 PENDING 质押不能激活")
        void cannot_activate_non_pending() {
            Pledge pledge = makePledge(1L, "ACTIVE");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            assertThatThrownBy(() -> pledgeService.activate(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有待生效的质押可以激活");
        }
    }

    // ── release ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("release - 解除质押")
    class Release {

        @Test
        @DisplayName("ACTIVE 质押可以解除")
        void release_active() {
            Pledge pledge = makePledge(1L, "ACTIVE");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge, pledge);
            when(pledgeMapper.updateById(any(Pledge.class))).thenReturn(1);

            pledgeService.release(1L);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("RELEASED");
            assertThat(captor.getValue().getEndDate()).isNotNull();
        }

        @Test
        @DisplayName("非 ACTIVE 质押不能解除")
        void cannot_release_non_active() {
            Pledge pledge = makePledge(1L, "PENDING");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            assertThatThrownBy(() -> pledgeService.release(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有生效中的质押可以解除");
        }
    }

    // ── markDefault ─────────────────────────────────────────────────

    @Nested
    @DisplayName("markDefault - 标记违约")
    class MarkDefault {

        @Test
        @DisplayName("ACTIVE 质押可以标记为违约")
        void mark_active_as_default() {
            Pledge pledge = makePledge(1L, "ACTIVE");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge, pledge);
            when(pledgeMapper.updateById(any(Pledge.class))).thenReturn(1);

            pledgeService.markDefault(1L);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).updateById(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo("DEFAULTED");
        }

        @Test
        @DisplayName("非 ACTIVE 质押不能标记违约")
        void cannot_mark_non_active_default() {
            Pledge pledge = makePledge(1L, "PENDING");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            assertThatThrownBy(() -> pledgeService.markDefault(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("只有生效中的质押可以标记为违约");
        }

        @Test
        @DisplayName("RELEASED 质押不能标记违约")
        void cannot_mark_released_default() {
            Pledge pledge = makePledge(1L, "RELEASED");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            assertThatThrownBy(() -> pledgeService.markDefault(1L))
                    .isInstanceOf(BizException.class);
        }

        @Test
        @DisplayName("已经违约的不能再次违约")
        void cannot_double_default() {
            Pledge pledge = makePledge(1L, "DEFAULTED");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            assertThatThrownBy(() -> pledgeService.markDefault(1L))
                    .isInstanceOf(BizException.class);
        }
    }

    // ── updatePledge ────────────────────────────────────────────────

    @Nested
    @DisplayName("updatePledge - 修改质押")
    class UpdatePledge {

        @Test
        @DisplayName("ACTIVE 质押不允许修改")
        void cannot_update_active() {
            Pledge pledge = makePledge(1L, "ACTIVE");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            Pledge update = new Pledge();
            assertThatThrownBy(() -> pledgeService.updatePledge(1L, update))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("生效中的质押不允许修改");
        }

        @Test
        @DisplayName("修改时清除 pledgeCode 防止覆盖")
        void update_clears_pledge_code() {
            Pledge pledge = makePledge(1L, "PENDING");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge, pledge);
            when(pledgeMapper.updateById(any(Pledge.class))).thenReturn(1);
            Pledge update = new Pledge();
            update.setPledgeCode("SHOULD_BE_CLEARED");
            update.setCommodity("Pear");

            pledgeService.updatePledge(1L, update);

            ArgumentCaptor<Pledge> captor = ArgumentCaptor.forClass(Pledge.class);
            verify(pledgeMapper).updateById(captor.capture());
            assertThat(captor.getValue().getPledgeCode()).isNull();
        }
    }

    // ── deletePledge ────────────────────────────────────────────────

    @Nested
    @DisplayName("deletePledge - 删除质押")
    class DeletePledge {

        @Test
        @DisplayName("ACTIVE 质押不能删除")
        void cannot_delete_active() {
            Pledge pledge = makePledge(1L, "ACTIVE");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            assertThatThrownBy(() -> pledgeService.deletePledge(1L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("生效中的质押不能删除");
        }

        @Test
        @DisplayName("PENDING 质押可以删除")
        void delete_pending() {
            Pledge pledge = makePledge(1L, "PENDING");
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);
            when(pledgeMapper.deleteById(1L)).thenReturn(1);

            pledgeService.deletePledge(1L);

            verify(pledgeMapper).deleteById(1L);
        }

        @Test
        @DisplayName("不存在的质押抛出异常")
        void throw_when_not_found() {
            when(pledgeMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> pledgeService.deletePledge(999L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("质押记录不存在");
        }
    }

    // ── getPledgeDetail ─────────────────────────────────────────────

    @Nested
    @DisplayName("getPledgeDetail - 查询质押详情")
    class GetPledgeDetail {

        @Test
        @DisplayName("存在时正常返回")
        void return_when_exists() {
            Pledge pledge = makePledge(1L, "ACTIVE");
            pledge.setAppraisedValue(new BigDecimal("500000"));
            when(pledgeMapper.selectById(1L)).thenReturn(pledge);

            Pledge result = pledgeService.getPledgeDetail(1L);

            assertThat(result).isNotNull();
            assertThat(result.getAppraisedValue()).isEqualByComparingTo("500000");
        }

        @Test
        @DisplayName("不存在时抛出 BizException")
        void throw_when_not_found() {
            when(pledgeMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> pledgeService.getPledgeDetail(999L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("质押记录不存在");
        }
    }
}
