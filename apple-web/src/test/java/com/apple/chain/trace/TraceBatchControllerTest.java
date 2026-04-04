package com.apple.chain.trace;

import com.apple.chain.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TraceBatch endpoints.
 * Uses H2 in-memory database seeded by Flyway migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("溯源批次 API 集成测试")
class TraceBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/trace/batches - 分页列表")
    void testListBatches() throws Exception {
        mockMvc.perform(get("/api/trace/batches")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("POST /api/trace/batches - 创建批次，自动生成batchCode和blockchainHash")
    void testCreateBatch() throws Exception {
        Map<String, Object> request = Map.of(
                "orchardId", 20001,
                "orchardName", "张家湾苹果园",
                "harvestDate", LocalDate.now().toString(),
                "variety", "红富士",
                "grade", "A",
                "weight", 5000.0
        );

        mockMvc.perform(post("/api/trace/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.batchCode").isNotEmpty())
                .andExpect(jsonPath("$.data.blockchainHash").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("CREATED"));
    }

    @Test
    @DisplayName("PUT /api/trace/batches/{id}/status - 状态更新 CREATED→PROCESSING")
    void testUpdateBatchStatus() throws Exception {
        // Create batch first
        Map<String, Object> createReq = Map.of(
                "orchardId", 20001,
                "orchardName", "张家湾苹果园",
                "harvestDate", LocalDate.now().toString(),
                "variety", "红富士",
                "grade", "B",
                "weight", 3000.0
        );
        String createResponse = mockMvc.perform(post("/api/trace/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).path("data").path("id").longValue();

        // Update status
        mockMvc.perform(put("/api/trace/batches/" + id + "/status")
                        .param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("GET /api/trace/batches/scan/{batchCode} - 公众扫码，无需登录")
    void testScanByBatchCode() throws Exception {
        // Create a batch to get its code
        Map<String, Object> createReq = Map.of(
                "orchardId", 20001,
                "orchardName", "张家湾苹果园",
                "harvestDate", LocalDate.now().toString(),
                "variety", "嘎拉",
                "grade", "A",
                "weight", 2000.0
        );
        String createResponse = mockMvc.perform(post("/api/trace/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn().getResponse().getContentAsString();

        String batchCode = objectMapper.readTree(createResponse)
                .path("data").path("batchCode").asText();

        // Scan by batchCode
        mockMvc.perform(get("/api/trace/batches/scan/" + batchCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.batch").isNotEmpty())
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("POST /api/trace/batches/{id}/records - 添加溯源记录")
    void testAddTraceRecord() throws Exception {
        // Create batch
        Map<String, Object> createReq = Map.of(
                "orchardId", 20001,
                "harvestDate", LocalDate.now().toString(),
                "variety", "黄元帅",
                "grade", "A",
                "weight", 1500.0
        );
        String createResponse = mockMvc.perform(post("/api/trace/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn().getResponse().getContentAsString();

        Long batchId = objectMapper.readTree(createResponse).path("data").path("id").longValue();

        // Add record
        Map<String, Object> recordReq = Map.of(
                "stage", "HARVEST",
                "operator", "张大农",
                "operatorPhone", "13811111101",
                "location", "陕西省延安市洛川县",
                "temperature", 18.5,
                "humidity", 65.0,
                "remark", "采收完成，品质良好"
        );
        mockMvc.perform(post("/api/trace/batches/" + batchId + "/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recordReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.stage").value("HARVEST"));
    }

    @Test
    @DisplayName("GET /api/trace/batches/{id}/records - 查询批次记录")
    void testListBatchRecords() throws Exception {
        // Create batch and add a record
        Map<String, Object> createReq = Map.of(
                "orchardId", 20001,
                "harvestDate", LocalDate.now().toString(),
                "variety", "红富士",
                "grade", "C",
                "weight", 800.0
        );
        String createResponse = mockMvc.perform(post("/api/trace/batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn().getResponse().getContentAsString();

        Long batchId = objectMapper.readTree(createResponse).path("data").path("id").longValue();

        // Query records (should be empty initially)
        mockMvc.perform(get("/api/trace/batches/" + batchId + "/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/trace/batches - status 过滤")
    void testListBatchesWithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/trace/batches")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }
}
