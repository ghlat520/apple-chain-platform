package com.apple.chain.coldchain.controller;

import com.apple.chain.coldchain.dto.LogisticsFullVO;
import com.apple.chain.coldchain.service.LogisticsQueryService;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "物流全链路查询")
@RestController
@RequestMapping("/api/coldchain/logistics")
@RequiredArgsConstructor
public class LogisticsQueryController {

    private final LogisticsQueryService logisticsQueryService;

    @Operation(summary = "获取任务完整物流信息")
    @GetMapping("/{taskId}/full")
    public R<LogisticsFullVO> getFullLogistics(@PathVariable Long taskId) {
        return R.ok(logisticsQueryService.getFullLogistics(taskId));
    }
}
