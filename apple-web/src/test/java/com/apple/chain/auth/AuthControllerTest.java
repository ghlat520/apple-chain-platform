package com.apple.chain.auth;

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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Auth endpoints.
 * Uses H2 in-memory database seeded by Flyway migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("认证接口 集成测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/user/auth/login - 空请求体返回400或业务错误")
    void testLoginWithEmptyBody() throws Exception {
        mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Spring Boot @Valid returns 422 Unprocessable Entity; some configs return 400
                    if (status == 200) {
                        String body = result.getResponse().getContentAsString();
                        org.junit.jupiter.api.Assertions.assertFalse(
                                body.contains("\"code\":200"),
                                "Empty body login should not succeed");
                    } else {
                        org.junit.jupiter.api.Assertions.assertTrue(
                                status == 400 || status == 422,
                                "Expected 400 or 422 but got: " + status);
                    }
                });
    }

    @Test
    @DisplayName("POST /api/user/auth/login - 无效凭据返回401或业务错误码")
    void testLoginWithInvalidCredentials() throws Exception {
        Map<String, String> request = Map.of(
                "username", "nonexistent_user_xyz",
                "password", "wrong_password_xyz"
        );

        mockMvc.perform(post("/api/user/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    String body = result.getResponse().getContentAsString();
                    // Should not return successful login
                    boolean isSuccessResponse = status == 200 && body.contains("\"code\":200")
                            && body.contains("\"token\"");
                    org.junit.jupiter.api.Assertions.assertFalse(
                            isSuccessResponse,
                            "Login with invalid credentials should not succeed. Status: " + status + ", Body: " + body);
                });
    }

    @Test
    @DisplayName("POST /api/user/auth/logout - 退出登录返回200")
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/user/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
