-- =============================================================================
-- V22 - Seed data for tr_chain_record (M2 blockchain certification)
--
-- 5 records matching the trace chains in tr_trace_chain (V2 seed):
--   4001: SOLD       -> chain_status=1 (success), hash verified
--   4002: IN_TRANSIT -> chain_status=1 (success), hash verified
--   4003: HARVESTED  -> chain_status=0 (pending)
--   4004: IN_STORAGE -> chain_status=2 (failed), 2 retries, timeout error
--   4005: PLANTED    -> chain_status=3 (retrying), 1 retry, 503 error
--
-- NOTE: data_hash values are SHA-256 of the MySQL-stored JSON form
-- (MySQL JSON columns reorder keys alphabetically). These hashes match
-- exactly what the Java verifyByTraceCode endpoint will re-compute.
-- =============================================================================

-- 1. SOLD -> success
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4001, 'TC20260301001', 'BATCH', 3001,
    '{"batchNo": "TB20260301001", "orchardId": 1001, "status": "SOLD", "variety": "红富士"}',
    '05979363b1474c0862a3239ffd54a93f2e9c83e056d421a209447d8ae4dc0c70',
    'tx_oulink_7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8',
    1000001, 1, NULL, 0,
    '2026-03-01 10:30:00', '2026-03-01 10:30:01', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 2. IN_TRANSIT -> success
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4002, 'TC20260301002', 'BATCH', 3002,
    '{"batchNo": "TB20260301002", "orchardId": 1002, "status": "IN_TRANSIT", "variety": "嘎拉"}',
    '82d0df55e53ddfdb18ecf8e527159d5cce7e6f0212a7f8a458ec56aea46476c7',
    'tx_oulink_8g9h0i1j2k3l4m5n6o7p8q9r0s1t2u3v4w5x6y7z8a9b0c1d2e3f4g5h6i7j8k9',
    1000002, 1, NULL, 0,
    '2026-03-01 14:20:00', '2026-03-01 14:20:02', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 3. HARVESTED -> pending (waiting to upload)
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4003, 'TC20260302001', 'BATCH', 3003,
    '{"batchNo": "TB20260302001", "orchardId": 1003, "status": "HARVESTED", "variety": "黄元帅"}',
    '84110044aa646b288e931f5b611b654a739fb4b2751c0dd7bdd0686436ab28ea',
    NULL, NULL, 0, NULL, 0,
    '2026-03-02 09:15:00', '2026-03-02 09:15:00', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 4. IN_STORAGE -> failed (after 2 retries)
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4004, 'TC20260303001', 'BATCH', 3004,
    '{"batchNo": "TB20260303001", "orchardId": 1001, "status": "IN_STORAGE", "variety": "秦冠"}',
    '699ea96fde518ad9be55a2657cd4d72200de4dc6845881a965b4033984ccf2ff',
    NULL, NULL, 2, '连接奥链超时: Connection timed out', 2,
    '2026-03-03 11:00:00', '2026-03-03 11:05:00', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;

-- 5. PLANTED -> retrying
INSERT INTO tr_chain_record (id, trace_code, business_type, business_id, data_snapshot, data_hash,
    chain_tx_hash, chain_block_height, chain_status, error_msg, retry_count,
    create_time, update_time, create_by, deleted)
VALUES (4005, 'TC20260304001', 'BATCH', 3005,
    '{"batchNo": "TB20260304001", "orchardId": 1002, "status": "PLANTED", "variety": "红富士"}',
    '9ca6fc547e63fcc132808e2b886e50853cd3c8507d1f6e22f8fdd4a28c0fa5d7',
    NULL, NULL, 3, '奥链节点响应异常: HTTP 503', 1,
    '2026-03-04 16:45:00', '2026-03-04 16:50:00', 'system', 0)
ON DUPLICATE KEY UPDATE trace_code = trace_code;
