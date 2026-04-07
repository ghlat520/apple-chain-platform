package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.TradeMatch;

import java.util.List;

/**
 * M7 撮合服务.
 */
public interface MatchService {

    /**
     * Compute and persist top-K matches for a supply listing.
     * Existing rows for the same (supply_id, demand_id) are upserted with the new score.
     */
    List<TradeMatch> computeForSupply(Long supplyId, int topK);

    /** Return top-K cached matches for a supply. */
    List<TradeMatch> topForSupply(Long supplyId, int topK);
}
