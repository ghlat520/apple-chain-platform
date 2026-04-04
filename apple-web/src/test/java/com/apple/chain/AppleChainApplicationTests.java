package com.apple.chain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application context load test.
 * Verifies that the Spring application context starts successfully with H2 in-memory DB.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@DisplayName("Application Context Load Test")
class AppleChainApplicationTests {

    @Test
    @DisplayName("Spring context loads successfully")
    void contextLoads() {
        // If the context fails to load this test will fail with an exception.
        // No assertions needed — the test framework handles it.
    }
}
