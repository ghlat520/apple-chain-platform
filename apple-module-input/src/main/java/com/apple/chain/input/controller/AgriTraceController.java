package com.apple.chain.input.controller;

import com.apple.chain.common.result.R;
import com.apple.chain.input.dto.AgriTraceChainVO;
import com.apple.chain.input.service.AgriTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Agricultural input traceability chain endpoints.
 */
@Tag(name = "农资溯源链路")
@RestController
@RequestMapping("/api/input/trace")
@RequiredArgsConstructor
public class AgriTraceController {

    private final AgriTraceService agriTraceService;

    @Operation(summary = "根据溯源码查询完整溯源链路")
    @GetMapping("/{traceCode}")
    public R<AgriTraceChainVO> getTraceChain(@PathVariable String traceCode) {
        return R.ok(agriTraceService.getTraceChain(traceCode));
    }
}
