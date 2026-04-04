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
 * Integration tests for Trade Order endpoints.
 * Uses H2 in-memory database seeded by Flyway V1+V2+V3 migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("交易订单 API 集成测试")
class TradeOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> buildOrderRequest() {
        Map<String, Object> req = new HashMap<>();
        req.put("variety", "红富士");
        req.put("quantity", 500.0);
        req.put("unitPrice", 4.6);
        req.put("totalAmount", 2300.0);
        req.put("buyerId", 1005);
        req.put("tradeDate", LocalDate.now().toString());
        req.put("orderStatus", "DRAFT");
        req.put("paymentStatus", "PENDING");
        return req;
    }

    private Long createOrder() throws Exception {
        String created = mockMvc.perform(post("/api/trade/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildOrderRequest())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(created).path("data").path("id").longValue();
    }

    @Test
    @DisplayName("GET /api/trade/order/list - 分页列表")
    void testListOrders() throws Exception {
        mockMvc.perform(get("/api/trade/order/list")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    @DisplayName("GET /api/trade/order/list - status 过滤")
    void testListOrdersWithStatusFilter() throws Exception {
        mockMvc.perform(get("/api/trade/order/list")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("POST /api/trade/order - 创建交易订单")
    void testCreateOrder() throws Exception {
        Map<String, Object> request = buildOrderRequest();

        mockMvc.perform(post("/api/trade/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.variety").value("红富士"));
    }

    @Test
    @DisplayName("GET /api/trade/order/{id} - 订单详情")
    void testGetOrderDetail() throws Exception {
        Long id = createOrder();

        mockMvc.perform(get("/api/trade/order/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.variety").value("红富士"));
    }

    @Test
    @DisplayName("PUT /{id}/confirm → /{id}/deliver → /{id}/complete - 订单状态流转")
    void testOrderStatusTransitions() throws Exception {
        Long id = createOrder();

        // Confirm
        mockMvc.perform(put("/api/trade/order/" + id + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderStatus").value("CONFIRMED"));

        // Deliver
        mockMvc.perform(put("/api/trade/order/" + id + "/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderStatus").value("DELIVERED"));

        // Complete
        mockMvc.perform(put("/api/trade/order/" + id + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("PUT /api/trade/order/{id}/cancel - 取消订单")
    void testCancelOrder() throws Exception {
        Long id = createOrder();

        mockMvc.perform(put("/api/trade/order/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"));
    }
}
