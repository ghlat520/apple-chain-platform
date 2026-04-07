package com.apple.chain.trade.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trade.entity.TradeMatch;
import com.apple.chain.trade.entity.TradeNegotiation;
import com.apple.chain.trade.entity.TradeOrder;
import com.apple.chain.trade.mapper.TradeMatchMapper;
import com.apple.chain.trade.mapper.TradeNegotiationMapper;
import com.apple.chain.trade.mapper.TradeOrderMapper;
import com.apple.chain.trade.service.NegotiationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NegotiationServiceImpl implements NegotiationService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TradeNegotiationMapper negotiationMapper;
    private final TradeMatchMapper tradeMatchMapper;
    private final TradeOrderMapper tradeOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeNegotiation start(Long matchId, Long supplyUserId, Long demandUserId,
                                  BigDecimal initialPrice, BigDecimal initialQuantity) {
        if (matchId == null || supplyUserId == null || demandUserId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "参数不能为空");
        }
        TradeMatch match = tradeMatchMapper.selectById(matchId);
        if (match == null) {
            throw new BizException(ResultCode.NOT_FOUND, "撮合不存在: " + matchId);
        }

        TradeNegotiation neg = new TradeNegotiation();
        neg.setMatchId(matchId);
        neg.setSupplyUserId(supplyUserId);
        neg.setDemandUserId(demandUserId);
        neg.setCurrentPrice(initialPrice);
        neg.setCurrentQuantity(initialQuantity);
        neg.setLastOfferBy(supplyUserId);
        neg.setStatus(TradeNegotiation.STATUS_ONGOING);
        negotiationMapper.insert(neg);

        // Bump the match status forward to NEGOTIATE
        match.setStatus(TradeMatch.STATUS_NEGOTIATE);
        tradeMatchMapper.updateById(match);
        return neg;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeNegotiation offer(Long negotiationId, Long byUserId, BigDecimal price, BigDecimal quantity) {
        TradeNegotiation neg = loadOngoing(negotiationId);
        if (byUserId == null
                || (!byUserId.equals(neg.getSupplyUserId()) && !byUserId.equals(neg.getDemandUserId()))) {
            throw new BizException(ResultCode.FORBIDDEN, "用户不在本议价会话中");
        }
        if (price == null || price.signum() <= 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "报价必须为正数");
        }
        neg.setCurrentPrice(price);
        if (quantity != null && quantity.signum() > 0) {
            neg.setCurrentQuantity(quantity);
        }
        neg.setLastOfferBy(byUserId);
        negotiationMapper.updateById(neg);
        return neg;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder accept(Long negotiationId, Long byUserId) {
        TradeNegotiation neg = loadOngoing(negotiationId);
        if (byUserId == null
                || (!byUserId.equals(neg.getSupplyUserId()) && !byUserId.equals(neg.getDemandUserId()))) {
            throw new BizException(ResultCode.FORBIDDEN, "用户不在本议价会话中");
        }
        // The accepting user must be the OPPOSITE side of the last offer
        if (byUserId.equals(neg.getLastOfferBy())) {
            throw new BizException(ResultCode.PARAM_ERROR, "不能接受自己的报价");
        }
        if (neg.getCurrentPrice() == null || neg.getCurrentPrice().signum() <= 0) {
            throw new BizException(ResultCode.PARAM_ERROR, "尚未有有效报价");
        }

        neg.setStatus(TradeNegotiation.STATUS_DEALT);
        negotiationMapper.updateById(neg);

        TradeMatch match = tradeMatchMapper.selectById(neg.getMatchId());
        if (match != null) {
            match.setStatus(TradeMatch.STATUS_DEALT);
            tradeMatchMapper.updateById(match);
        }

        // Create the trade order. Schema is the existing td_trade_order from V1.
        // Field mapping: supplyUserId → farmerId (seller side), demandUserId → buyerId.
        TradeOrder order = new TradeOrder();
        String prefix = LocalDate.now().format(DATE_FMT);
        int seq = tradeOrderMapper.nextSeq(prefix);
        order.setOrderNo(String.format("ORD%s%04d", prefix, seq));
        order.setBuyerId(neg.getDemandUserId());
        order.setFarmerId(neg.getSupplyUserId());
        order.setQuantity(neg.getCurrentQuantity());
        order.setUnitPrice(neg.getCurrentPrice());
        BigDecimal qty = neg.getCurrentQuantity() == null ? BigDecimal.ZERO : neg.getCurrentQuantity();
        order.setTotalAmount(neg.getCurrentPrice().multiply(qty));
        order.setOrderStatus("DRAFT");
        order.setPaymentStatus("PENDING");
        order.setTradeDate(LocalDate.now());
        order.setRemark("由 M7 议价 #" + negotiationId + " 自动生成");
        tradeOrderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeNegotiation cancel(Long negotiationId, Long byUserId) {
        TradeNegotiation neg = loadOngoing(negotiationId);
        if (byUserId == null
                || (!byUserId.equals(neg.getSupplyUserId()) && !byUserId.equals(neg.getDemandUserId()))) {
            throw new BizException(ResultCode.FORBIDDEN, "用户不在本议价会话中");
        }
        neg.setStatus(TradeNegotiation.STATUS_CANCELED);
        negotiationMapper.updateById(neg);
        return neg;
    }

    private TradeNegotiation loadOngoing(Long negotiationId) {
        TradeNegotiation neg = negotiationMapper.selectById(negotiationId);
        if (neg == null) {
            throw new BizException(ResultCode.NOT_FOUND, "议价会话不存在: " + negotiationId);
        }
        if (neg.getStatus() != TradeNegotiation.STATUS_ONGOING) {
            throw new BizException(ResultCode.PARAM_ERROR,
                    "议价会话状态非 ONGOING (status=" + neg.getStatus() + ")");
        }
        return neg;
    }
}
