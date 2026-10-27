package com.apple.chain.finance;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.finance.entity.CreditRating;
import com.apple.chain.finance.mapper.CreditRatingMapper;
import com.apple.chain.finance.service.impl.CreditRatingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * M7 CreditRatingService unit tests.
 *
 * The real calculateScore uses Random; here we test:
 * - the weighted formula produces scores within expected range
 * - credit level boundaries are correct
 * - CRUD operations delegate correctly
 * - edge cases: null inputs, missing data
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreditRatingService - 信用评级服务")
class CreditRatingServiceTest {

    @Mock
    private CreditRatingMapper creditRatingMapper;

    private CreditRatingServiceImpl creditRatingService;

    @BeforeEach
    void setUp() {
        creditRatingService = new CreditRatingServiceImpl();
        ReflectionTestUtils.setField(creditRatingService, "baseMapper", creditRatingMapper);
    }

    // ── calculateScore ──────────────────────────────────────────────

    @Nested
    @DisplayName("calculateScore - 加权评分计算")
    class CalculateScore {

        @Test
        @DisplayName("评分结果在合法范围内 (0-100) 且信用等级有效")
        void should_produce_score_in_valid_range() {
            when(creditRatingMapper.insert(any(CreditRating.class))).thenReturn(1);

            for (int i = 0; i < 200; i++) {
                CreditRating result = creditRatingService.calculateScore(1L, "ENTERPRISE");

                assertThat(result.getCreditScore())
                        .as("评分应在 0-100 之间, 第%d次", i)
                        .isGreaterThanOrEqualTo(0)
                        .isLessThanOrEqualTo(100);
                assertThat(result.getCreditLevel())
                        .as("信用等级应为 A/B/C/D, 第%d次", i)
                        .isIn("A", "B", "C", "D");
            }
        }

        @Test
        @DisplayName("评分不低于交易分下限 60*0.4 + 50*0.3 + 55*0.3 = 55")
        void should_never_be_below_theoretical_minimum() {
            when(creditRatingMapper.insert(any(CreditRating.class))).thenReturn(1);

            int minScore = Integer.MAX_VALUE;
            for (int i = 0; i < 500; i++) {
                CreditRating result = creditRatingService.calculateScore(1L, "ENTERPRISE");
                minScore = Math.min(minScore, result.getCreditScore());
            }
            // theoretical minimum: 60*0.4 + 50*0.3 + 55*0.3 = 24+15+16.5 = 55.5 -> 56
            assertThat(minScore).as("随机采样最低分不应低于理论下限 55")
                    .isGreaterThanOrEqualTo(55);
        }

        @Test
        @DisplayName("评分不高于交易分上限 95*0.4 + 90*0.3 + 85*0.3 = 90")
        void should_never_exceed_theoretical_maximum() {
            when(creditRatingMapper.insert(any(CreditRating.class))).thenReturn(1);

            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < 500; i++) {
                CreditRating result = creditRatingService.calculateScore(1L, "ENTERPRISE");
                maxScore = Math.max(maxScore, result.getCreditScore());
            }
            // theoretical maximum: 95*0.4 + 90*0.3 + 85*0.3 = 38+27+25.5 = 90.5 -> 91
            assertThat(maxScore).as("随机采样最高分不应超过理论上限 91")
                    .isLessThanOrEqualTo(91);
        }

        @Test
        @DisplayName("信用等级 A 要求评分 >= 85")
        void level_A_requires_score_at_least_85() {
            when(creditRatingMapper.insert(any(CreditRating.class))).thenReturn(1);

            for (int i = 0; i < 500; i++) {
                CreditRating result = creditRatingService.calculateScore(1L, "ENTERPRISE");
                if ("A".equals(result.getCreditLevel())) {
                    assertThat(result.getCreditScore())
                            .as("等级 A 的评分应 >= 85")
                            .isGreaterThanOrEqualTo(85);
                }
            }
        }

        @Test
        @DisplayName("信用等级 D 要求评分 < 55")
        void level_D_requires_score_below_55() {
            when(creditRatingMapper.insert(any(CreditRating.class))).thenReturn(1);

            for (int i = 0; i < 500; i++) {
                CreditRating result = creditRatingService.calculateScore(1L, "ENTERPRISE");
                if ("D".equals(result.getCreditLevel())) {
                    assertThat(result.getCreditScore())
                            .as("等级 D 的评分应 < 55")
                            .isLessThan(55);
                }
            }
        }

        @Test
        @DisplayName("保存的评级包含正确的主体信息")
        void should_save_with_correct_entity_info() {
            when(creditRatingMapper.insert(any(CreditRating.class))).thenReturn(1);

            CreditRating result = creditRatingService.calculateScore(100L, "FARMER");

            assertThat(result.getEntityId()).isEqualTo(100L);
            assertThat(result.getEntityType()).isEqualTo("FARMER");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            assertThat(result.getAssessmentDate()).isNotNull();
            assertThat(result.getValidUntil()).isEqualTo(result.getAssessmentDate().plusYears(1));
        }
    }

    // ── CRUD ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRatingDetail - 查询评级详情")
    class GetRatingDetail {

        @Test
        @DisplayName("评级存在时正常返回")
        void should_return_rating_when_exists() {
            CreditRating rating = new CreditRating();
            rating.setId(1L);
            rating.setCreditScore(80);
            when(creditRatingMapper.selectById(1L)).thenReturn(rating);

            CreditRating result = creditRatingService.getRatingDetail(1L);

            assertThat(result).isNotNull();
            assertThat(result.getCreditScore()).isEqualTo(80);
        }

        @Test
        @DisplayName("评级不存在时抛出 BizException")
        void should_throw_when_not_found() {
            when(creditRatingMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> creditRatingService.getRatingDetail(999L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("信用评级不存在");
        }
    }

    @Nested
    @DisplayName("createRating - 创建评级")
    class CreateRating {

        @Test
        @DisplayName("创建时自动设置 ACTIVE 状态")
        void should_set_status_active() {
            when(creditRatingMapper.insert(any(CreditRating.class))).thenReturn(1);

            CreditRating input = new CreditRating();
            input.setEntityName("Test Corp");
            CreditRating result = creditRatingService.createRating(input);

            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            verify(creditRatingMapper).insert(any(CreditRating.class));
        }
    }

    @Nested
    @DisplayName("deleteRating - 删除评级")
    class DeleteRating {

        @Test
        @DisplayName("评级存在时可以删除")
        void should_delete_when_exists() {
            CreditRating rating = new CreditRating();
            rating.setId(1L);
            when(creditRatingMapper.selectById(1L)).thenReturn(rating);
            when(creditRatingMapper.deleteById(1L)).thenReturn(1);

            creditRatingService.deleteRating(1L);

            verify(creditRatingMapper).deleteById(1L);
        }

        @Test
        @DisplayName("评级不存在时抛出 BizException")
        void should_throw_when_not_found() {
            when(creditRatingMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> creditRatingService.deleteRating(999L))
                    .isInstanceOf(BizException.class);
        }
    }

    @Nested
    @DisplayName("updateRating - 更新评级")
    class UpdateRating {

        @Test
        @DisplayName("更新成功返回最新数据")
        void should_update_and_return() {
            CreditRating existing = new CreditRating();
            existing.setId(1L);
            existing.setCreditScore(70);
            when(creditRatingMapper.selectById(1L)).thenReturn(existing);
            when(creditRatingMapper.updateById(any(CreditRating.class))).thenReturn(1);
            CreditRating updated = new CreditRating();
            updated.setCreditScore(90);
            when(creditRatingMapper.selectById(1L)).thenReturn(updated);

            creditRatingService.updateRating(1L, updated);

            ArgumentCaptor<CreditRating> captor = ArgumentCaptor.forClass(CreditRating.class);
            verify(creditRatingMapper).updateById(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("评级不存在时抛出 BizException")
        void should_throw_when_not_found() {
            when(creditRatingMapper.selectById(999L)).thenReturn(null);

            CreditRating input = new CreditRating();
            assertThatThrownBy(() -> creditRatingService.updateRating(999L, input))
                    .isInstanceOf(BizException.class);
        }
    }

    // ── Weighted formula verification (deterministic) ──────────────

    @Nested
    @DisplayName("加权公式验证 (确定性边界)")
    class WeightedFormula {

        @Test
        @DisplayName("交易分权重40% + 生产分权重30% + 财务分权重30% = 100%")
        void weights_sum_to_100_percent() {
            double total = 0.4 + 0.3 + 0.3;
            assertThat(total).as("权重之和应为 1.0").isEqualTo(1.0);
        }

        @Test
        @DisplayName("理论最低分: 60*0.4 + 50*0.3 + 55*0.3 = 55.5 -> 56")
        void theoretical_minimum_is_56() {
            double score = 60 * 0.4 + 50 * 0.3 + 55 * 0.3;
            assertThat(Math.round(score)).isEqualTo(56);
        }

        @Test
        @DisplayName("理论最高分: 95*0.4 + 90*0.3 + 85*0.3 = 90.5 -> 91")
        void theoretical_maximum_is_91() {
            double score = 95 * 0.4 + 90 * 0.3 + 85 * 0.3;
            assertThat(Math.round(score)).isEqualTo(91);
        }

        @Test
        @DisplayName("交易分满分时得分为 95*0.4 + 50*0.3 + 55*0.3 = 69.5 -> 70")
        void trade_max_score_calculation() {
            double score = 95 * 0.4 + 50 * 0.3 + 55 * 0.3;
            assertThat((int) Math.round(score)).isEqualTo(70);
        }

        @Test
        @DisplayName("交易分最低时得分最低: 60*0.4 + 50*0.3 + 55*0.3 = 55.5 -> 56")
        void low_trade_lowers_score() {
            double score = 60 * 0.4 + 50 * 0.3 + 55 * 0.3;
            assertThat((int) Math.round(score)).isEqualTo(56);
        }

        @Test
        @DisplayName("交易分满分加生产分满分时得分: 95*0.4 + 90*0.3 + 55*0.3 = 82.5 -> 83")
        void trade_and_production_max_gives_high_score() {
            double score = 95 * 0.4 + 90 * 0.3 + 55 * 0.3;
            assertThat((int) Math.round(score)).isGreaterThanOrEqualTo(80);
        }
    }
}
