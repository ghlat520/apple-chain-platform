package com.apple.chain.trace.controller;

import com.apple.chain.common.result.R;
import com.apple.chain.trace.entity.TraceCode;
import com.apple.chain.trace.service.TraceCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * M12 — Three-level traceability code endpoints.
 * Path: /api/trace/code
 */
@Tag(name = "M12 三级溯源码")
@RestController
@RequestMapping("/api/trace/code")
@RequiredArgsConstructor
public class TraceCodeController {

    private final TraceCodeService traceCodeService;

    @Operation(summary = "生成 BOX 级编码")
    @PostMapping("/generate-box")
    public R<List<TraceCode>> generateBox(
            @RequestParam Long batchId,
            @RequestParam int boxCount) {
        return R.ok(traceCodeService.generateBoxCodes(batchId, boxCount));
    }

    @Operation(summary = "生成 FRUIT 级编码")
    @PostMapping("/generate-fruit")
    public R<List<TraceCode>> generateFruit(
            @RequestParam String boxCode,
            @RequestParam int fruitCount) {
        return R.ok(traceCodeService.generateFruitCodes(boxCode, fruitCount));
    }

    @Operation(summary = "校验溯源码（CRC16 + 状态）")
    @GetMapping("/verify/{code}")
    public R<Map<String, Object>> verify(@PathVariable String code) {
        return R.ok(traceCodeService.verifyCode(code));
    }

    @Operation(summary = "VDP 印刷文件导出（csv/txt）")
    @GetMapping("/vdp-export/{batchId}")
    public void exportVdp(
            @PathVariable Long batchId,
            @RequestParam(defaultValue = "csv") String format,
            HttpServletResponse response) {
        traceCodeService.exportVdpFile(batchId, format, response);
    }
}
