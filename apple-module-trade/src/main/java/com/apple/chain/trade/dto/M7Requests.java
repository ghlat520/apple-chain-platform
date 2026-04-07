package com.apple.chain.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * M7 request DTOs grouped in one file (small + tightly coupled).
 */
public final class M7Requests {

    private M7Requests() {
    }

    @Data
    public static class StartNegotiationRequest {
        @NotNull private Long matchId;
        @NotNull private Long supplyUserId;
        @NotNull private Long demandUserId;
        @NotNull @Positive private BigDecimal price;
        @NotNull @Positive private BigDecimal quantity;
    }

    @Data
    public static class OfferRequest {
        @NotNull private Long byUserId;
        @NotNull @Positive private BigDecimal price;
        private BigDecimal quantity;
    }

    @Data
    public static class AcceptRequest {
        @NotNull private Long byUserId;
    }

    @Data
    public static class SendChatRequest {
        @NotNull @Size(min = 1, max = 64) private String sessionId;
        @NotNull private Long fromUserId;
        @NotNull private Long toUserId;
        @Size(max = 16)  private String msgType;
        @NotNull @Size(min = 1, max = 4096) private String content;
    }
}
