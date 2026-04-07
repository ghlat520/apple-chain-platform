package com.apple.chain.trace.service;

import com.apple.chain.trace.entity.TraceCode;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * M12 — Three-level traceability code service.
 * <p>
 * Code format:
 *   BATCH  : {batchCode}                           (reuses TraceBatch.batchCode)
 *   BOX    : {batchCode}-B{seq3}-{CRC4}
 *   FRUIT  : {boxCode}-F{seq4}-{CRC4}
 */
public interface TraceCodeService extends IService<TraceCode> {

    /**
     * Generate N BOX codes under a given batch.
     *
     * @param batchId  trace_batch.id
     * @param boxCount number of boxes (1..10000)
     * @return newly generated BOX codes (in generation order)
     */
    List<TraceCode> generateBoxCodes(Long batchId, int boxCount);

    /**
     * Generate N FRUIT codes under a given BOX code.
     *
     * @param boxCode    parent BOX code
     * @param fruitCount number of fruits per box (1..1000)
     * @return newly generated FRUIT codes (in generation order)
     */
    List<TraceCode> generateFruitCodes(String boxCode, int fruitCount);

    /**
     * Verify a code's CRC16 checksum and look up its status.
     *
     * @param code raw code (BATCH/BOX/FRUIT)
     * @return verification result: {valid, granularity, status, parentCode, batchId, message}
     */
    Map<String, Object> verifyCode(String code);

    /**
     * Export VDP (Variable Data Printing) file for the given batch.
     * Format: CSV or TXT. Contains one row per code that can be fed to a
     * label-printing device (e.g. Domino / Videojet).
     */
    void exportVdpFile(Long batchId, String format, HttpServletResponse response);
}
