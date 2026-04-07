package com.apple.chain.common.config;

import com.apple.chain.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cross-cutting config endpoint for the 高德 (AMap) Web JS API.
 * <p>
 * Used by M4 (orchard map) and M3 (logistics tracking). Centralized so the
 * key/secret can be rotated server-side via env var without a frontend rebuild.
 * <p>
 * Security note: AMap's Web JSAPI 2.0 model places both `key` and `securityJsCode`
 * in the browser; they are protected by referrer whitelist on the AMap side, not
 * by being kept secret. We still avoid hardcoding them in the repo so:
 *   1. They never appear in git history (rotation = redeploy with new env var)
 *   2. Different envs (dev/staging/prod) use different keys with different referrers
 *
 * Both values are read from configuration:
 *   amap.web-jsapi-key:    ${AMAP_WEB_JSAPI_KEY:}
 *   amap.web-jsapi-secret: ${AMAP_WEB_JSAPI_SECRET:}
 *
 * Empty values are returned as empty strings so the frontend can detect a
 * missing config and prompt the operator.
 */
@Tag(name = "前端配置")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class AmapConfigController {

    @Value("${amap.web-jsapi-key:}")
    private String webJsApiKey;

    @Value("${amap.web-jsapi-secret:}")
    private String webJsApiSecret;

    @Operation(summary = "高德 Web JSAPI Key 与签名 Code", description = "前端 amap-jsapi-loader 使用")
    @GetMapping("/amap")
    public R<Map<String, String>> amap() {
        // LinkedHashMap to preserve order in JSON output
        Map<String, String> body = new LinkedHashMap<>();
        body.put("key", webJsApiKey == null ? "" : webJsApiKey);
        body.put("securityJsCode", webJsApiSecret == null ? "" : webJsApiSecret);
        return R.ok(body);
    }
}
