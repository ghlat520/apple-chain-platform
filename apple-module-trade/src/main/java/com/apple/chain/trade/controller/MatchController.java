package com.apple.chain.trade.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.result.R;
import com.apple.chain.trade.dto.M7Requests.*;
import com.apple.chain.trade.entity.ChatMessage;
import com.apple.chain.trade.entity.TradeMatch;
import com.apple.chain.trade.entity.TradeNegotiation;
import com.apple.chain.trade.entity.TradeOrder;
import com.apple.chain.trade.service.ChatService;
import com.apple.chain.trade.service.MatchService;
import com.apple.chain.trade.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * M7 endpoints: matching + negotiation + chat.
 * REST-only — WebSocket push is deferred (offline persistence via DB is enough
 * to satisfy the M7 acceptance criteria).
 */
@Tag(name = "M7 撮合议价")
@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final NegotiationService negotiationService;
    private final ChatService chatService;

    // ===== Matching =====

    @Operation(summary = "为某供应计算 top-K 撮合候选")
    @PostMapping("/match/compute")
    @RequirePerm("trade:write")
    public R<List<TradeMatch>> compute(@RequestParam Long supplyId,
                                        @RequestParam(defaultValue = "10") int topK) {
        return R.ok(matchService.computeForSupply(supplyId, topK));
    }

    @Operation(summary = "查询某供应的 top-K 缓存撮合")
    @GetMapping("/match/supply/{supplyId}/top")
    @RequirePerm("trade:read")
    public R<List<TradeMatch>> top(@PathVariable Long supplyId,
                                    @RequestParam(defaultValue = "10") int topK) {
        return R.ok(matchService.topForSupply(supplyId, topK));
    }

    // ===== Negotiation =====

    @Operation(summary = "发起议价")
    @PostMapping("/negotiation/start")
    @RequirePerm("trade:write")
    public R<TradeNegotiation> startNeg(@Valid @RequestBody StartNegotiationRequest req) {
        return R.ok(negotiationService.start(
                req.getMatchId(), req.getSupplyUserId(), req.getDemandUserId(),
                req.getPrice(), req.getQuantity()));
    }

    @Operation(summary = "报价/还价")
    @PostMapping("/negotiation/{id}/offer")
    @RequirePerm("trade:write")
    public R<TradeNegotiation> offer(@PathVariable Long id, @Valid @RequestBody OfferRequest req) {
        return R.ok(negotiationService.offer(id, req.getByUserId(), req.getPrice(), req.getQuantity()));
    }

    @Operation(summary = "接受当前报价 → 自动建单")
    @PostMapping("/negotiation/{id}/accept")
    @RequirePerm("trade:write")
    public R<TradeOrder> accept(@PathVariable Long id, @Valid @RequestBody AcceptRequest req) {
        return R.ok(negotiationService.accept(id, req.getByUserId()));
    }

    @Operation(summary = "取消议价")
    @PostMapping("/negotiation/{id}/cancel")
    @RequirePerm("trade:write")
    public R<TradeNegotiation> cancel(@PathVariable Long id, @Valid @RequestBody AcceptRequest req) {
        return R.ok(negotiationService.cancel(id, req.getByUserId()));
    }

    // ===== Chat =====

    @Operation(summary = "发送聊天消息")
    @PostMapping("/chat/send")
    @RequirePerm("trade:write")
    public R<ChatMessage> sendChat(@Valid @RequestBody SendChatRequest req) {
        return R.ok(chatService.send(
                req.getSessionId(), req.getFromUserId(), req.getToUserId(),
                req.getMsgType(), req.getContent()));
    }

    @Operation(summary = "拉取会话历史消息")
    @GetMapping("/chat/{sessionId}/history")
    @RequirePerm("trade:read")
    public R<List<ChatMessage>> chatHistory(@PathVariable String sessionId,
                                             @RequestParam(defaultValue = "100") int limit) {
        return R.ok(chatService.history(sessionId, limit));
    }

    @Operation(summary = "标记会话内消息为已读")
    @PostMapping("/chat/{sessionId}/read")
    @RequirePerm("trade:read")
    public R<Integer> markRead(@PathVariable String sessionId, @RequestParam Long userId) {
        return R.ok(chatService.markRead(sessionId, userId));
    }
}
