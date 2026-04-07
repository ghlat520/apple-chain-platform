package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdAuditLog;
import com.apple.chain.bigdata.service.BdAuditLogService;
import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Big-data admin audit-log query.
 */
@Tag(name = "大数据-审计日志")
@RestController
@RequestMapping("/api/bigdata/audit")
@RequiredArgsConstructor
public class AuditLogQueryController {

    private final BdAuditLogService service;

    @Operation(summary = "审计日志查询")
    @GetMapping
    public R<PageResult<BdAuditLog>> query(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(PageResult.of(service.query(username, module, action, page, size)));
    }
}
