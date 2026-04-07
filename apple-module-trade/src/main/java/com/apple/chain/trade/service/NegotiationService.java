package com.apple.chain.trade.service;

import com.apple.chain.trade.entity.TradeNegotiation;
import com.apple.chain.trade.entity.TradeOrder;

import java.math.BigDecimal;

/**
 * M7 议价 service. State machine: ONGOING → DEALT or CANCELED.
 *
 * <p>{@code accept} produces a {@link TradeOrder} side-effect via the existing
 * order creation flow.
 */
public interface NegotiationService {

    /** Start a negotiation session for an existing match. */
    TradeNegotiation start(Long matchId, Long supplyUserId, Long demandUserId,
                           BigDecimal initialPrice, BigDecimal initialQuantity);

    /** Counter-offer: updates current price/quantity, records the offering user. */
    TradeNegotiation offer(Long negotiationId, Long byUserId, BigDecimal price, BigDecimal quantity);

    /** Accept the current offer → DEALT, creates a TradeOrder. */
    TradeOrder accept(Long negotiationId, Long byUserId);

    /** Cancel the negotiation. */
    TradeNegotiation cancel(Long negotiationId, Long byUserId);
}
