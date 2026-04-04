package com.apple.chain.farm;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Orchard CRUD endpoints.
 * Uses H2 in-memory database seeded by Flyway V1+V2 migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("果园管理 API 集成测试")
class OrchardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/farm/orchards - 分页列表")
    void testListOrchards() throws Exception {
        mockMvc.perform(get("/api/farm/orchards")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("GET /api/farm/orchards - keyword 搜索")
    void testListOrchardsWithKeyword() throws Exception {
        mockMvc.perform(get("/api/farm/orchards")
                        .param("keyword", "张家湾"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /api/farm/orchards - status 过滤")
    void testListOrchardsWithStatus() throws Exception {
        mockMvc.perform(get("/api/farm/orchards")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /api/farm/orchards - 创建果园")
    void testCreateOrchard() throws Exception {
        Map<String, Object> request = Map.of(
                "orchardName", "测试新果园",
                "farmerId", 10001,
                "location", "陕西省延安市测试镇",
                "area", 50.0,
                "variety", "红富士",
                "plantingYear", 2023,
                "status", "ACTIVE"
        );

        MvcResult result = mockMvc.perform(post("/api/farm/orchards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orchardCode").isNotEmpty())
                .andExpect(jsonPath("$.data.orchardName").value("测试新果园"))
                .andReturn();
    }

    @Test
    @DisplayName("GET /api/farm/orchards/{id} - 详情")
    void testGetOrchardById() throws Exception {
        // First create one
        Map<String, Object> request = Map.of(
                "orchardName", "详情测试果园",
                "farmerId", 10001,
                "location", "陕西省",
                "area", 30.0,
                "variety", "嘎拉",
                "status", "ACTIVE"
        );

        String createResponse = mockMvc.perform(post("/api/farm/orchards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse)
                .path("data").path("id").longValue();

        // Then get by id
        mockMvc.perform(get("/api/farm/orchards/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orchardName").value("详情测试果园"));
    }

    @Test
    @DisplayName("PUT /api/farm/orchards/{id} - 更新果园")
    void testUpdateOrchard() throws Exception {
        // Create
        Map<String, Object> createReq = Map.of(
                "orchardName", "更新前果园名",
                "farmerId", 10001,
                "location", "陕西省",
                "area", 30.0,
                "variety", "秦冠",
                "status", "ACTIVE"
        );
        String created = mockMvc.perform(post("/api/farm/orchards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).path("data").path("id").longValue();

        // Update
        Map<String, Object> updateReq = Map.of(
                "orchardName", "更新后果园名",
                "farmerId", 10001,
                "status", "INACTIVE"
        );
        mockMvc.perform(put("/api/farm/orchards/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orchardName").value("更新后果园名"));
    }

    @Test
    @DisplayName("DELETE /api/farm/orchards/{id} - 软删除")
    void testDeleteOrchard() throws Exception {
        // Create
        Map<String, Object> request = Map.of(
                "orchardName", "待删除果园",
                "farmerId", 10001,
                "status", "ACTIVE"
        );
        String created = mockMvc.perform(post("/api/farm/orchards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(created).path("data").path("id").longValue();

        // Delete
        mockMvc.perform(delete("/api/farm/orchards/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify deleted (should return 404 or empty)
        mockMvc.perform(get("/api/farm/orchards/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
