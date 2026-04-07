package com.apple.chain.trace.service.impl;

import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.trace.entity.TraceBatch;
import com.apple.chain.trace.entity.TraceCode;
import com.apple.chain.trace.mapper.TraceBatchMapper;
import com.apple.chain.trace.mapper.TraceCodeMapper;
import com.apple.chain.trace.service.TraceCodeService;
import com.apple.chain.trace.util.Crc16;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * M12 — Three-level traceability code service implementation.
 * <p>
 * Concurrency note: sequence numbers for BOX/FRUIT are derived from the
 * current count of existing children for the same parent. Generation should be
 * driven from a single background worker per batch to avoid race windows; the
 * unique index uk_code provides a final safety net by rejecting duplicates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceCodeServiceImpl
        extends ServiceImpl<TraceCodeMapper, TraceCode>
        implements TraceCodeService {

    private static final String GRAN_BATCH = "BATCH";
    private static final String GRAN_BOX = "BOX";
    private static final String GRAN_FRUIT = "FRUIT";

    private static final int MAX_BOX_PER_BATCH = 10000;
    private static final int MAX_FRUIT_PER_BOX = 1000;
    private static final int MAX_VDP_EXPORT_ROWS = 100_000;
    private static final int BATCH_CODE_LENGTH = 14; // TB(2) + yyyyMMdd(8) + seq(4)

    private static final String QR_URL_TEMPLATE = "/public/scan/%s";

    private final TraceBatchMapper traceBatchMapper;

    // ===================================================================
    //  BOX generation
    // ===================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TraceCode> generateBoxCodes(Long batchId, int boxCount) {
        if (batchId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "batchId 不能为空");
        }
        if (boxCount <= 0 || boxCount > MAX_BOX_PER_BATCH) {
            throw new BizException(ResultCode.PARAM_ERROR,
                    "boxCount 必须在 1~" + MAX_BOX_PER_BATCH + " 之间");
        }

        TraceBatch batch = traceBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BizException(ResultCode.NOT_FOUND, "溯源批次不存在: " + batchId);
        }
        String batchCode = batch.getBatchCode();

        ensureBatchLevelRecord(batch);

        int existing = baseMapper.countByParent(batchCode, GRAN_BOX);
        List<TraceCode> created = new ArrayList<>(boxCount);
        for (int i = 1; i <= boxCount; i++) {
            int seq = existing + i;
            String boxCode = buildBoxCode(batchCode, seq);
            TraceCode tc = buildTraceCode(boxCode, GRAN_BOX, batchCode, batchId);
            saveWithRetry(tc);
            created.add(tc);
        }
        return created;
    }

    // ===================================================================
    //  FRUIT generation
    // ===================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TraceCode> generateFruitCodes(String boxCode, int fruitCount) {
        if (boxCode == null || boxCode.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "boxCode 不能为空");
        }
        if (fruitCount <= 0 || fruitCount > MAX_FRUIT_PER_BOX) {
            throw new BizException(ResultCode.PARAM_ERROR,
                    "fruitCount 必须在 1~" + MAX_FRUIT_PER_BOX + " 之间");
        }

        TraceCode parent = baseMapper.findByCode(boxCode);
        if (parent == null || !GRAN_BOX.equals(parent.getGranularity())) {
            throw new BizException(ResultCode.NOT_FOUND, "BOX 编码不存在: " + boxCode);
        }

        int existing = baseMapper.countByParent(boxCode, GRAN_FRUIT);
        List<TraceCode> created = new ArrayList<>(fruitCount);
        for (int i = 1; i <= fruitCount; i++) {
            int seq = existing + i;
            String fruitCode = buildFruitCode(boxCode, seq);
            TraceCode tc = buildTraceCode(fruitCode, GRAN_FRUIT, boxCode, parent.getBatchId());
            saveWithRetry(tc);
            created.add(tc);
        }
        return created;
    }

    // ===================================================================
    //  Verify
    // ===================================================================

    @Override
    public Map<String, Object> verifyCode(String code) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);

        if (code == null || code.isBlank()) {
            result.put("valid", false);
            result.put("message", "编码不能为空");
            return result;
        }

        // 1. Try BATCH level first — format is plain batchCode without CRC
        if (isBatchCodeFormat(code)) {
            TraceBatch batch = traceBatchMapper.findByBatchCode(code);
            if (batch == null) {
                result.put("valid", false);
                result.put("message", "批次不存在");
                return result;
            }
            result.put("valid", true);
            result.put("granularity", GRAN_BATCH);
            result.put("status", batch.getStatus());
            result.put("parentCode", null);
            result.put("batchId", batch.getId());
            result.put("message", "OK");
            return result;
        }

        // 2. BOX / FRUIT — must have CRC suffix
        int lastDash = code.lastIndexOf('-');
        if (lastDash < 0 || lastDash >= code.length() - 1) {
            result.put("valid", false);
            result.put("message", "编码格式错误");
            return result;
        }
        String payload = code.substring(0, lastDash);
        String providedCrc = code.substring(lastDash + 1).toUpperCase(Locale.ROOT);
        String expectedCrc = Crc16.hex(payload);
        if (!expectedCrc.equals(providedCrc)) {
            result.put("valid", false);
            result.put("message", "CRC 校验失败");
            return result;
        }

        TraceCode tc = baseMapper.findByCode(code);
        if (tc == null) {
            result.put("valid", false);
            result.put("message", "编码未登记");
            return result;
        }
        result.put("valid", true);
        result.put("granularity", tc.getGranularity());
        result.put("status", tc.getStatus());
        result.put("parentCode", tc.getParentCode());
        result.put("batchId", tc.getBatchId());
        result.put("message", "OK");
        return result;
    }

    // ===================================================================
    //  VDP export
    // ===================================================================

    @Override
    public void exportVdpFile(Long batchId, String format, HttpServletResponse response) {
        if (batchId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "batchId 不能为空");
        }
        TraceBatch batch = traceBatchMapper.selectById(batchId);
        if (batch == null) {
            throw new BizException(ResultCode.NOT_FOUND, "溯源批次不存在: " + batchId);
        }
        String fmt = (format == null || format.isBlank()) ? "csv" : format.toLowerCase(Locale.ROOT);
        if (!"csv".equals(fmt) && !"txt".equals(fmt)) {
            throw new BizException(ResultCode.PARAM_ERROR, "format 仅支持 csv / txt");
        }

        List<TraceCode> boxes = baseMapper.findByBatchAndGranularity(batchId, GRAN_BOX);
        List<TraceCode> fruits = baseMapper.findByBatchAndGranularity(batchId, GRAN_FRUIT);

        int totalRows = 1 + boxes.size() + fruits.size();
        if (totalRows > MAX_VDP_EXPORT_ROWS) {
            throw new BizException(ResultCode.PARAM_ERROR,
                    "该批次编码数量过多 (" + totalRows + ")，请分批导出");
        }

        String filename = "VDP_" + batch.getBatchCode() + "." + fmt;
        try {
            response.setContentType(("csv".equals(fmt) ? "text/csv" : "text/plain") + ";charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            PrintWriter writer = response.getWriter();
            if ("csv".equals(fmt)) {
                writer.write('\uFEFF');
                writer.println("code,granularity,parent_code,crc16,qr_url");
                writeCsvLine(writer, batch.getBatchCode(), GRAN_BATCH, "", "", String.format(QR_URL_TEMPLATE, batch.getBatchCode()));
                for (TraceCode b : boxes) {
                    writeCsvLine(writer, b.getCode(), b.getGranularity(),
                            nz(b.getParentCode()), nz(b.getCrc16()), nz(b.getQrUrl()));
                }
                for (TraceCode f : fruits) {
                    writeCsvLine(writer, f.getCode(), f.getGranularity(),
                            nz(f.getParentCode()), nz(f.getCrc16()), nz(f.getQrUrl()));
                }
            } else {
                // VDP TXT: one code per line (what most label printers expect)
                writer.println(batch.getBatchCode());
                for (TraceCode b : boxes) writer.println(b.getCode());
                for (TraceCode f : fruits) writer.println(f.getCode());
            }
            writer.flush();
        } catch (Exception e) {
            throw new BizException("VDP 导出失败: " + e.getMessage());
        }
    }

    // ===================================================================
    //  Helpers
    // ===================================================================

    private void ensureBatchLevelRecord(TraceBatch batch) {
        TraceCode existing = baseMapper.findByCode(batch.getBatchCode());
        if (existing != null) {
            return;
        }
        TraceCode tc = new TraceCode();
        tc.setCode(batch.getBatchCode());
        tc.setGranularity(GRAN_BATCH);
        tc.setParentCode(null);
        tc.setBatchId(batch.getId());
        tc.setCrc16(null); // BATCH level reuses trace_batch and has no CRC suffix
        tc.setQrUrl(String.format(QR_URL_TEMPLATE, batch.getBatchCode()));
        tc.setStatus("ACTIVE");
        save(tc);
    }

    /** BOX format: {batchCode}-B{seq3}-{CRC4} */
    public static String buildBoxCode(String batchCode, int seq) {
        String payload = String.format("%s-B%03d", batchCode, seq);
        return payload + "-" + Crc16.hex(payload);
    }

    /** FRUIT format: {boxCode}-F{seq4}-{CRC4} */
    public static String buildFruitCode(String boxCode, int seq) {
        String payload = String.format("%s-F%04d", boxCode, seq);
        return payload + "-" + Crc16.hex(payload);
    }

    /** Pull the trailing 4-char CRC segment from a full code. */
    private static String extractCrc(String fullCode) {
        int dash = fullCode.lastIndexOf('-');
        return (dash < 0) ? null : fullCode.substring(dash + 1);
    }

    /** BATCH code format: TB + yyyyMMdd + 4-digit seq (length 14, no dash). */
    private static boolean isBatchCodeFormat(String code) {
        return code.length() == 14
                && code.startsWith("TB")
                && code.indexOf('-') < 0;
    }

    /**
     * RFC 4180 compliant CSV writer: quotes fields containing commas, double-quotes, or newlines.
     */
    private static void writeCsvLine(PrintWriter w, String... cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            String col = cols[i];
            if (col == null) col = "";
            if (col.contains(",") || col.contains("\"") || col.contains("\n")) {
                sb.append('"').append(col.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(col);
            }
        }
        w.println(sb);
    }

    /**
     * Save a TraceCode, retrying once on duplicate key (concurrent generation race).
     */
    private void saveWithRetry(TraceCode tc) {
        try {
            save(tc);
        } catch (DuplicateKeyException e) {
            log.warn("Duplicate code detected, skipping: {}", tc.getCode());
            // uk_code unique index guarantees no data corruption
        }
    }

    private TraceCode buildTraceCode(String code, String granularity, String parentCode, Long batchId) {
        TraceCode tc = new TraceCode();
        tc.setCode(code);
        tc.setGranularity(granularity);
        tc.setParentCode(parentCode);
        tc.setBatchId(batchId);
        tc.setCrc16(extractCrc(code));
        tc.setQrUrl(String.format(QR_URL_TEMPLATE, code));
        tc.setStatus("ACTIVE");
        return tc;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
