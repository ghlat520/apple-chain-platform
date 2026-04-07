package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.PurchaseNeed;
import com.apple.chain.trade.entity.SupplyInfo;
import com.apple.chain.trade.entity.TradeMatch;
import com.apple.chain.trade.mapper.PurchaseNeedMapper;
import com.apple.chain.trade.mapper.SupplyInfoMapper;
import com.apple.chain.trade.mapper.TradeMatchMapper;
import com.apple.chain.trade.match.MatchAlgorithm;
import com.apple.chain.trade.service.MatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private static final int MAX_TOP_K = 100;

    private final SupplyInfoMapper supplyInfoMapper;
    private final PurchaseNeedMapper purchaseNeedMapper;
    private final TradeMatchMapper tradeMatchMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TradeMatch> computeForSupply(Long supplyId, int topK) {
        if (supplyId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "supplyId 不能为空");
        }
        int safeK = Math.min(Math.max(topK, 1), MAX_TOP_K);

        SupplyInfo supply = supplyInfoMapper.selectById(supplyId);
        if (supply == null) {
            throw new BizException(ResultCode.NOT_FOUND, "supply 不存在: " + supplyId);
        }

        // Pull all PUBLISHED demands for the same variety; in real life this would
        // pre-filter by region/window, but simple "WHERE variety AND status" is
        // enough at the volumes M7 targets (thousands of demands, not millions).
        List<PurchaseNeed> demands = purchaseNeedMapper.selectList(
                new LambdaQueryWrapper<PurchaseNeed>()
                        .eq(PurchaseNeed::getVariety, supply.getVariety())
                        .eq(PurchaseNeed::getStatus, "PUBLISHED"));

        // Score, sort, take top-K
        record Scored(PurchaseNeed demand, BigDecimal score) {}
        List<Scored> scored = new ArrayList<>(demands.size());
        for (PurchaseNeed d : demands) {
            BigDecimal s = MatchAlgorithm.score(supply, d);
            if (s.signum() > 0) {
                scored.add(new Scored(d, s));
            }
        }
        scored.sort(Comparator.<Scored, BigDecimal>comparing(s -> s.score).reversed());
        if (scored.size() > safeK) {
            scored = scored.subList(0, safeK);
        }

        List<TradeMatch> persisted = new ArrayList<>(scored.size());
        for (Scored s : scored) {
            TradeMatch tm = upsertMatch(supplyId, s.demand().getId(), s.score());
            persisted.add(tm);
        }
        return persisted;
    }

    @Override
    public List<TradeMatch> topForSupply(Long supplyId, int topK) {
        int safeK = Math.min(Math.max(topK, 1), MAX_TOP_K);
        return tradeMatchMapper.findTopForSupply(supplyId, safeK);
    }

    /**
     * Upsert (supply_id, demand_id) → score. The unique index protects against
     * concurrent recomputes by raising DuplicateKeyException, which we then
     * resolve via an UPDATE.
     */
    private TradeMatch upsertMatch(Long supplyId, Long demandId, BigDecimal score) {
        TradeMatch tm = new TradeMatch();
        tm.setSupplyId(supplyId);
        tm.setDemandId(demandId);
        tm.setMatchScore(score);
        tm.setMatchTime(LocalDateTime.now());
        tm.setStatus(TradeMatch.STATUS_CANDIDATE);
        try {
            tradeMatchMapper.insert(tm);
            return tm;
        } catch (DuplicateKeyException e) {
            TradeMatch existing = tradeMatchMapper.selectOne(
                    new LambdaQueryWrapper<TradeMatch>()
                            .eq(TradeMatch::getSupplyId, supplyId)
                            .eq(TradeMatch::getDemandId, demandId)
                            .last("LIMIT 1"));
            if (existing != null) {
                existing.setMatchScore(score);
                existing.setMatchTime(LocalDateTime.now());
                tradeMatchMapper.updateById(existing);
                return existing;
            }
            throw e;
        }
    }
}
