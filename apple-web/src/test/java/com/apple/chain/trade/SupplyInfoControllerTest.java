package com.apple.chain.trade;

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
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Supply Info endpoints.
 * Uses H2 in-memory database seeded by Flyway V1+V2+V3 migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("供货管理 API 集成测试")
class SupplyInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> buildSupplyRequest() {
        Map<String, Object> req = new HashMap<>();
        req.put("farmerId", 10001);
        req.put("variety", "红富士");
        req.put("quantity", 5000.0);
        req.put("priceExpected", 4.5);
        req.put("harvestDate", LocalDate.now().toString());
        req.put("location", "陕西省延安市洛川县");
        req.put("quality", "A");
        req.put("status", "DRAFT");
        return req;
    }

    @Test
    @DisplayName("GET /api/trade/supply/list - 分页列表")
    void testListSupplies() throws Exception {
        mockMvc.perform(get("/api/trade/supply/list")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("GET /api/trade/supply/list - 品种过滤")
    void testListSuppliesWithFilter() throws Exception {
        mockMvc.perform(get("/api/trade/supply/list")
                        .param("variety", "红富士"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("POST /api/trade/supply - 创建供货信息")
    void testCreateSupply() throws Exception {
        Map<String, Object> request = buildSupplyRequest();

        mockMvc.perform(post("/api/trade/supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.variety").value("红富士"));
    }

    @Test
    @DisplayName("GET /api/trade/supply/{id} - 详情")
    void testGetSupplyDetail() throws Exception {
        // Create first
        String created = mockMvc.perform(post("/api/trade/supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSupplyRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).path("data").path("id").longValue();

        // Get detail
        mockMvc.perform(get("/api/trade/supply/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.variety").value("红富士"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    @DisplayName("PUT /api/trade/supply/{id} - 更新供货信息")
    void testUpdateSupply() throws Exception {
        // Create first
        String created = mockMvc.perform(post("/api/trade/supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSupplyRequest())))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).path("data").path("id").longValue();

        // Update
        Map<String, Object> updateReq = buildSupplyRequest();
        updateReq.put("variety", "嘎拉");
        updateReq.put("quantity", 3000.0);

        mockMvc.perform(put("/api/trade/supply/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.variety").value("嘎拉"));
    }

    @Test
    @DisplayName("DELETE /api/trade/supply/{id} - 删除供货信息")
    void testDeleteSupply() throws Exception {
        // Create first
        String created = mockMvc.perform(post("/api/trade/supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSupplyRequest())))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).path("data").path("id").longValue();

        // Delete
        mockMvc.perform(delete("/api/trade/supply/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/trade/supply/{id}/publish - 发布供货信息")
    void testPublishSupply() throws Exception {
        // Create first
        String created = mockMvc.perform(post("/api/trade/supply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildSupplyRequest())))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).path("data").path("id").longValue();

        // Publish
        mockMvc.perform(put("/api/trade/supply/" + id + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }
}
