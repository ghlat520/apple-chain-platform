package com.apple.chain.finance.dto;

import com.apple.chain.finance.enums.RiskEventStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Payload for handling (closing) a risk event.
 *
 * <p>{@code status} is restricted by the service to one of
 * {@code HANDLING / RESOLVED / IGNORED}.
 */
@Getter
@Setter
public class RiskEventHandleRequest {

    @NotNull(message = "目标状态不能为空")
    private RiskEventStatus status;

    @Size(max = 500, message = "处置备注最长500字")
    private String handleRemark;

    /** Optional assignee user id. */
    private Long assigneeId;
}
