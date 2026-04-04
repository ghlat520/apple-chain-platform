package com.apple.chain.trace.controller;

import com.apple.chain.common.result.PageResult;
import com.apple.chain.common.result.R;
import com.apple.chain.trace.entity.TraceChain;
import com.apple.chain.trace.entity.TraceNode;
import com.apple.chain.trace.service.TraceService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Trace chain and public scan endpoints.
 */
@Tag(name = "产品溯源")
@RestController
@RequestMapping("/api/trace")
@RequiredArgsConstructor
public class TraceController {

    private final TraceService traceService;

    @Operation(summary = "溯源链列表（分页）")
    @GetMapping("/list")
    public R<PageResult<TraceChain>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return R.ok(PageResult.of(traceService.listChains(page, size, keyword, status)));
    }

    @Operation(summary = "溯源详情（含时间轴节点）")
    @GetMapping("/{traceCode}")
    public R<Map<String, Object>> detail(@PathVariable String traceCode) {
        return R.ok(traceService.getTraceDetail(traceCode));
    }

    @Operation(summary = "公众扫码查询（无需登录）")
    @GetMapping("/scan/{traceCode}")
    public R<Map<String, Object>> scan(@PathVariable String traceCode) {
        return R.ok(traceService.publicScan(traceCode));
    }

    @Operation(summary = "生成溯源二维码PNG")
    @GetMapping(value = "/qrcode/{traceCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrcode(@PathVariable String traceCode,
                         @RequestParam(defaultValue = "300") int size) throws Exception {
        // Verify trace code exists
        traceService.getTraceDetail(traceCode);
        // Generate QR code pointing to public scan URL
        String scanUrl = "/scan/" + traceCode;
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(scanUrl, BarcodeFormat.QR_CODE, size, size);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    @Operation(summary = "添加溯源节点")
    @PostMapping("/node")
    public R<TraceNode> addNode(@RequestBody TraceNode node) {
        return R.ok(traceService.addNode(node));
    }

    @Operation(summary = "导出溯源链CSV")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        traceService.exportChains(keyword, status, response);
    }
}
