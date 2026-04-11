package com.apple.chain.bigdata.controller;

import com.apple.chain.bigdata.entity.BdApiCallLog;
import com.apple.chain.bigdata.entity.BdApiClient;
import com.apple.chain.bigdata.service.BdApiClientService;
import com.apple.chain.common.result.R;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Open API client management controller.
 * Path: /api/bigdata/openapi/clients
 */
@Tag(name = "开放API客户端管理")
@RestController
@RequestMapping("/api/bigdata/openapi/clients")
@RequiredArgsConstructor
public class OpenApiController {

    private final BdApiClientService apiClientService;

    @Operation(summary = "注册新API客户端")
    @PostMapping
    public R<BdApiClient> register(@RequestBody BdApiClient client) {
        // Generate appKey and a placeholder encrypted secret
        client.setAppKey(generateAppKey());
        client.setAppSecretEnc(generateSecret());
        client.setStatus("ACTIVE");
        apiClientService.save(client);
        return R.ok(client);
    }

    @Operation(summary = "分页查询API客户端列表")
    @GetMapping
    public R<IPage<BdApiClient>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(apiClientService.page(new Page<>(page, size)));
    }

    @Operation(summary = "更新客户端状态（ACTIVE/DISABLED/REVOKED）")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        BdApiClient client = apiClientService.getById(id);
        if (client == null) {
            return R.fail("客户端不存在");
        }
        client.setStatus(status);
        apiClientService.updateById(client);
        return R.ok();
    }

    @Operation(summary = "查询客户端调用日志")
    @GetMapping("/{id}/logs")
    public R<IPage<BdApiCallLog>> logs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        BdApiClient client = apiClientService.getById(id);
        if (client == null) {
            return R.fail("客户端不存在");
        }
        return R.ok(apiClientService.listCallLogs(client.getAppKey(), page, size));
    }

    @Operation(summary = "轮转密钥（重新生成 appKey + appSecretEnc）")
    @PostMapping("/{id}/rotate-key")
    public R<BdApiClient> rotateKey(@PathVariable Long id) {
        BdApiClient client = apiClientService.getById(id);
        if (client == null) {
            return R.fail("客户端不存在");
        }
        client.setAppKey(generateAppKey());
        client.setAppSecretEnc(generateSecret());
        apiClientService.updateById(client);
        // Return updated client (secret visible only at this moment)
        return R.ok(client);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private String generateAppKey() {
        return "ak_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateSecret() {
        // Placeholder — production should use AES encryption with a key from config
        return "enc_" + UUID.randomUUID().toString().replace("-", "");
    }
}
