package com.apple.chain.bigdata;

import com.apple.chain.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Dashboard / BigData endpoints.
 * All endpoints are read-only aggregations against H2 seeded data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("数据大屏 API 集成测试")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/bigdata/dashboard/stats - KPI汇总统计")
    void testGetStats() throws Exception {
        mockMvc.perform(get("/api/bigdata/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("GET /api/bigdata/dashboard/harvest-trend - 采收趋势")
    void testGetHarvestTrend() throws Exception {
        mockMvc.perform(get("/api/bigdata/dashboard/harvest-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/bigdata/dashboard/trade-trend - 交易趋势")
    void testGetTradeTrend() throws Exception {
        mockMvc.perform(get("/api/bigdata/dashboard/trade-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/bigdata/dashboard/variety-distribution - 品种分布")
    void testGetVarietyDistribution() throws Exception {
        mockMvc.perform(get("/api/bigdata/dashboard/variety-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/bigdata/dashboard/top-orchards - TOP5果园")
    void testGetTopOrchards() throws Exception {
        mockMvc.perform(get("/api/bigdata/dashboard/top-orchards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/bigdata/dashboard/supply-demand - 供需对比")
    void testGetSupplyDemand() throws Exception {
        mockMvc.perform(get("/api/bigdata/dashboard/supply-demand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/bigdata/dashboard/trace-stats - 溯源链状态分布")
    void testGetTraceStats() throws Exception {
        mockMvc.perform(get("/api/bigdata/dashboard/trace-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
