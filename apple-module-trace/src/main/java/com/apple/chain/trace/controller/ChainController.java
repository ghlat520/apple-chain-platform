package com.apple.chain.trace.controller;

import com.apple.chain.common.auth.RequirePerm;
import com.apple.chain.common.result.R;
import com.apple.chain.trace.dto.ChainSubmitRequest;
import com.apple.chain.trace.entity.ChainRecord;
import com.apple.chain.trace.service.ChainSubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * M2 chain endpoints.
 *
 * <p>Path layout:
 * <ul>
 *   <li>{@code POST /api/trace/chain/submit} — internal: queue a snapshot for upload</li>
 *   <li>{@code GET  /api/trace/chain/verify/{traceCode}} — public: verify chain hash</li>
 *   <li>{@code GET  /api/admin/chain/records} — admin: list records by status</li>
 *   <li>{@code POST /api/admin/chain/retry/{id}} — admin: manually retry a failed record</li>
 * </ul>
 */
@Tag(name = "M2 区块链存证")
@RestController
@RequiredArgsConstructor
public class ChainController {

    private final ChainSubmitService chainSubmitService;

    @Operation(summary = "提交业务快照上链", description = "内部接口，自动哈希后异步上链")
    @PostMapping("/api/trace/chain/submit")
    @RequirePerm("trace:write")
    public R<ChainRecord> submit(@Valid @RequestBody ChainSubmitRequest req) {
        ChainRecord record = chainSubmitService.submit(
                req.getTraceCode(), req.getBusinessType(), req.getBusinessId(), req.getSnapshot());
        return R.ok(record);
    }

    @Operation(summary = "公开校验某溯源码的链上记录")
    @GetMapping("/api/trace/chain/verify/{traceCode}")
    public R<Map<String, Object>> verify(@PathVariable String traceCode) {
        return R.ok(chainSubmitService.verifyByTraceCode(traceCode));
    }

    @Operation(summary = "管理：上链记录列表", description = "status: 0待上链 1成功 2失败 3重试中")
    @GetMapping("/api/admin/chain/records")
    @RequirePerm("trace:read")
    public R<List<ChainRecord>> records(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "100") int limit) {
        return R.ok(chainSubmitService.listByStatus(status, limit));
    }

    @Operation(summary = "管理：手动重试上链失败的记录")
    @PostMapping("/api/admin/chain/retry/{id}")
    @RequirePerm("trace:write")
    public R<ChainRecord> retry(@PathVariable Long id) {
        return R.ok(chainSubmitService.retry(id));
    }
}
