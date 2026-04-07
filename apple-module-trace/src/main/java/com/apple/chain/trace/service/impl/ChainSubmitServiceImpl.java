package com.apple.chain.trace.service.impl;

import com.apple.chain.chain.client.ChainUploadResult;
import com.apple.chain.chain.client.OulinkChainClient;
import com.apple.chain.common.exception.BizException;
import com.apple.chain.common.result.ResultCode;
import com.apple.chain.common.util.Sha256Util;
import com.apple.chain.trace.entity.ChainRecord;
import com.apple.chain.trace.mapper.ChainRecordMapper;
import com.apple.chain.trace.service.ChainSubmitService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * M2 chain submit service.
 *
 * <p>Flow:
 * <pre>
 *   submit(): persist record (PENDING) → return record → trigger async upload
 *   doUploadAsync(): call OulinkChainClient → update record (SUCCESS or FAILED/RETRY)
 *   retry(): synchronous re-attempt; admin-driven
 *   verifyByTraceCode(): re-hash current snapshot, compare with stored hash
 * </pre>
 *
 * <p>Why @Async (not RocketMQ as the original plan suggested): the project does
 * not use RocketMQ. Spring's @Async gives the same fire-and-forget semantics
 * with zero new infrastructure. When the real Aochain SDK arrives and the chain
 * load grows, this can be swapped for an MQ-based pattern without touching the
 * caller (TraceCodeServiceImpl).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChainSubmitServiceImpl implements ChainSubmitService {

    private final ChainRecordMapper chainRecordMapper;
    private final OulinkChainClient chainClient;
    private final ApplicationEventPublisher eventPublisher;

    /** Internal event used to cross the proxy boundary so @Async actually fires. */
    public record ChainUploadRequestedEvent(Long recordId) {
    }

    /** Stable JSON serializer: keys sorted so the same logical state always hashes the same. */
    private static final ObjectMapper STABLE_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChainRecord submit(String traceCode, String businessType, Long businessId, Map<String, Object> snapshot) {
        if (traceCode == null || traceCode.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "traceCode 不能为空");
        }
        if (businessType == null || businessType.isBlank()) {
            throw new BizException(ResultCode.PARAM_ERROR, "businessType 不能为空");
        }
        if (snapshot == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "snapshot 不能为空");
        }

        String canonicalJson = toCanonicalJson(snapshot);
        String dataHash = Sha256Util.hashHex(canonicalJson);

        ChainRecord record = new ChainRecord();
        record.setTraceCode(traceCode);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setDataSnapshot(canonicalJson);
        record.setDataHash(dataHash);
        record.setChainStatus(ChainRecord.STATUS_PENDING);
        record.setRetryCount(0);
        chainRecordMapper.insert(record);

        // Publish event AFTER the enclosing transaction commits so the async
        // listener sees the committed row. Going through ApplicationEventPublisher
        // also crosses the Spring proxy boundary, which is what makes @Async work.
        eventPublisher.publishEvent(new ChainUploadRequestedEvent(record.getId()));
        return record;
    }

    /**
     * Async upload listener. {@link TransactionalEventListener#phase()} = AFTER_COMMIT
     * guarantees we never try to read a row that the surrounding transaction
     * later rolls back. {@link Async} pushes execution onto the task executor.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChainUploadRequested(ChainUploadRequestedEvent event) {
        ChainRecord record = chainRecordMapper.selectById(event.recordId());
        if (record == null) {
            log.warn("Chain record {} disappeared before upload", event.recordId());
            return;
        }
        doUpload(record);
    }

    private void doUpload(ChainRecord record) {
        // Build minimal metadata so verify endpoints can correlate without huge payload
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("traceCode", record.getTraceCode());
        meta.put("businessType", record.getBusinessType());
        meta.put("dataHash", record.getDataHash());
        String metaJson = toCanonicalJson(meta);

        ChainUploadResult result;
        try {
            result = chainClient.uploadHash(record.getDataHash(), metaJson);
        } catch (Exception e) {
            log.error("Chain upload threw for record {}: {}", record.getId(), e.toString());
            result = ChainUploadResult.fail(e.getMessage());
        }

        if (result.success()) {
            record.setChainTxHash(result.txHash());
            record.setChainBlockHeight(result.blockHeight());
            record.setChainStatus(ChainRecord.STATUS_SUCCESS);
            record.setErrorMsg(null);
        } else {
            int retries = record.getRetryCount() == null ? 0 : record.getRetryCount();
            if (retries >= ChainRecord.MAX_RETRY) {
                record.setChainStatus(ChainRecord.STATUS_FAILED);
            } else {
                record.setChainStatus(ChainRecord.STATUS_RETRY);
            }
            record.setErrorMsg(truncate(result.errorMessage(), 512));
        }
        chainRecordMapper.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChainRecord retry(Long chainRecordId) {
        ChainRecord record = chainRecordMapper.selectById(chainRecordId);
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND, "chain record 不存在: " + chainRecordId);
        }
        if (record.getChainStatus() == ChainRecord.STATUS_SUCCESS) {
            return record;
        }
        record.setRetryCount((record.getRetryCount() == null ? 0 : record.getRetryCount()) + 1);
        doUpload(record); // synchronous for admin button
        return record;
    }

    @Override
    public Map<String, Object> verifyByTraceCode(String traceCode) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("traceCode", traceCode);

        ChainRecord record = chainRecordMapper.findLatestByTraceCode(traceCode);
        if (record == null) {
            result.put("valid", false);
            result.put("message", "未找到上链记录");
            return result;
        }

        // Re-hash the stored snapshot and compare with the dataHash field as a
        // tamper-detection signal — useful if someone modified tr_chain_record
        // directly in the DB after the record was written.
        String currentHash = Sha256Util.hashHex(record.getDataSnapshot());
        boolean hashMatch = currentHash.equals(record.getDataHash());

        result.put("valid", hashMatch && record.getChainStatus() == ChainRecord.STATUS_SUCCESS);
        result.put("hashMatch", hashMatch);
        result.put("chainStatus", record.getChainStatus());
        result.put("chainTxHash", record.getChainTxHash());
        result.put("chainBlockHeight", record.getChainBlockHeight());
        result.put("dataHash", record.getDataHash());
        result.put("currentHash", currentHash);
        return result;
    }

    @Override
    public List<ChainRecord> listByStatus(Integer status, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return chainRecordMapper.findByStatus(status, safeLimit);
    }

    /** Canonical JSON: sorted keys → stable hash regardless of map insertion order. */
    private static String toCanonicalJson(Map<String, Object> snapshot) {
        try {
            // Wrap in TreeMap to enforce ordering even for nested LinkedHashMaps
            return STABLE_MAPPER.writeValueAsString(new TreeMap<>(snapshot));
        } catch (JsonProcessingException e) {
            throw new BizException(ResultCode.FAIL, "snapshot 序列化失败: " + e.getMessage());
        }
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
