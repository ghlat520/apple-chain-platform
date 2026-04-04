package com.apple.chain;

import com.apple.chain.common.util.JwtUtil;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Test configuration.
 * Replaces Redis beans with mocks so tests run without a Redis server.
 * Adds a default Authorization header to all MockMvc requests via MockMvcBuilderCustomizer.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return Mockito.mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        return Mockito.mock(RedisTemplate.class);
    }

    /**
     * Adds a valid test JWT to every MockMvc request so JWT-protected endpoints return 200.
     * Uses userId=1, username="testadmin", roleCode="ADMIN".
     */
    @Bean
    public MockMvcBuilderCustomizer authMockMvcCustomizer(JwtUtil jwtUtil) {
        String token = jwtUtil.generateToken(1L, "testadmin", "ADMIN");
        return builder -> builder.defaultRequest(
                MockMvcRequestBuilders.get("/")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        );
    }
}
